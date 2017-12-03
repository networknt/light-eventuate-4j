package com.networknt.eventuate.jdbc;

import com.networknt.eventuate.common.Aggregate;
import com.networknt.eventuate.common.DuplicateTriggeringEventException;
import com.networknt.eventuate.common.EntityIdAndType;
import com.networknt.eventuate.common.EntityNotFoundException;
import com.networknt.eventuate.common.EventContext;
import com.networknt.eventuate.common.Int128;
import com.networknt.eventuate.common.OptimisticLockingException;
import com.networknt.eventuate.common.impl.AggregateCrudFindOptions;
import com.networknt.eventuate.common.impl.AggregateCrudSaveOptions;
import com.networknt.eventuate.common.impl.AggregateCrudUpdateOptions;
import com.networknt.eventuate.common.impl.EntityIdVersionAndEventIds;
import com.networknt.eventuate.common.impl.EventIdTypeAndData;
import com.networknt.eventuate.common.impl.EventTypeAndData;
import com.networknt.eventuate.common.impl.LoadedEvents;
import com.networknt.eventuate.common.impl.SerializedSnapshot;
import com.networknt.eventuate.common.impl.SerializedSnapshotWithVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class EventuateJdbcAccessImpl implements EventuateJdbcAccess {

  public static final String DEFAULT_DATABASE_SCHEMA = "eventuate";

  protected Logger logger = LoggerFactory.getLogger(getClass());

  private DataSource dataSource;
  private String entityTable;
  private String eventTable;
  private String snapshotTable;

  public EventuateJdbcAccessImpl(DataSource dataSource) {

    this(dataSource, new EventuateSchema());
  }

  public EventuateJdbcAccessImpl(DataSource dataSource, EventuateSchema eventuateSchema) {
    this.dataSource = dataSource;

    entityTable = eventuateSchema.qualifyTable("entities");
    eventTable = eventuateSchema.qualifyTable("events");
    snapshotTable = eventuateSchema.qualifyTable("snapshots");
  }

  private IdGenerator idGenerator = new IdGeneratorImpl();

  @Override
  public SaveUpdateResult save(String aggregateType, List<EventTypeAndData> events, Optional<AggregateCrudSaveOptions> saveOptions) {
    List<EventIdTypeAndData> eventsWithIds = events.stream().map(this::toEventWithId).collect(Collectors.toList());
    String entityId = saveOptions.flatMap(AggregateCrudSaveOptions::getEntityId).orElse(idGenerator.genId().asString());

    Int128 entityVersion = last(eventsWithIds).getId();

    String insert_entities = String.format("INSERT INTO %s (entity_type, entity_id, entity_version) VALUES (?, ?, ?)", entityTable);
    String insert_events = String.format("INSERT INTO %s (event_id, event_type, event_data, entity_type, entity_id, triggering_event, metadata) VALUES (?, ?, ?, ?, ?, ?, ?)", eventTable);

    try (final Connection connection = dataSource.getConnection()) {
      connection.setAutoCommit(false);
      try (PreparedStatement psEntity = connection.prepareStatement(insert_entities)) {
        psEntity.setString(1, aggregateType);
        psEntity.setString(2, entityId);
        psEntity.setString(3, entityVersion.asString());
        psEntity.executeUpdate();
      }

      try (PreparedStatement psEvent = connection.prepareStatement(insert_events)) {
        for(EventIdTypeAndData event : eventsWithIds) {
          psEvent.setString(1, event.getId().asString());
          psEvent.setString(2, event.getEventType());
          psEvent.setString(3, event.getEventData());
          psEvent.setString(4, aggregateType);
          psEvent.setString(5, entityId);
          psEvent.setString(6, saveOptions.flatMap(AggregateCrudSaveOptions::getTriggeringEvent).map(EventContext::getEventToken).orElse(null));
          psEvent.setString(7, event.getMetadata().orElse(null));
          psEvent.addBatch();
        }
        psEvent.executeBatch();
      }
      connection.commit();
    } catch (SQLException e) {
      // the rollback is called automatically by the HikariCP
      logger.error("SqlException:", e);
    }

    return new SaveUpdateResult(new EntityIdVersionAndEventIds(entityId, entityVersion, eventsWithIds.stream().map(EventIdTypeAndData::getId).collect(Collectors.toList())),
            new PublishableEvents(aggregateType, entityId, eventsWithIds));
  }


  private <T> T last(List<T> eventsWithIds) {
    return eventsWithIds.get(eventsWithIds.size() - 1);
  }

  private EventIdTypeAndData toEventWithId(EventTypeAndData eventTypeAndData) {
    return new EventIdTypeAndData(idGenerator.genId(), eventTypeAndData.getEventType(), eventTypeAndData.getEventData(), eventTypeAndData.getMetadata());
  }

  private Optional<LoadedSnapshot> getSnapshot(String aggregateType, String entityId) {
    LoadedSnapshot snapshot = null;
    try (final Connection connection = dataSource.getConnection()){
      String psSelect = String.format("select snapshot_type, snapshot_json, entity_version, triggering_Events from %s where entity_type = ? and entity_id = ? order by entity_version desc LIMIT 1", snapshotTable);
      try (PreparedStatement stmt = connection.prepareStatement(psSelect)) {
        stmt.setString(1, aggregateType);
        stmt.setString(2, entityId);

        try (ResultSet rs = stmt.executeQuery()) {
          while (rs.next()) {
            snapshot = new LoadedSnapshot(new SerializedSnapshotWithVersion(
                    new SerializedSnapshot(rs.getString("snapshot_type"), rs.getString("snapshot_json"))
                    , Int128.fromString(rs.getString("entity_version"))), rs.getString("triggering_events"));
          }
        }
      }
    } catch (SQLException e) {
      logger.error("SqlException:", e);
    }
    if (snapshot!=null ) {
      return Optional.ofNullable(snapshot);
    } else {
      return Optional.empty();
    }
  }

  @Override
  public <T extends Aggregate<T>> LoadedEvents find(String aggregateType, String entityId, Optional<AggregateCrudFindOptions> findOptions) {
    Optional<LoadedSnapshot> snapshot = getSnapshot(aggregateType, entityId);
    snapshot.ifPresent(ss -> {
      findOptions.flatMap(AggregateCrudFindOptions::getTriggeringEvent).ifPresent(te -> {
        checkSnapshotForDuplicateEvent(ss, te);
      });
    });

    List<EventAndTrigger> events = new ArrayList<>();

    try (final Connection connection = dataSource.getConnection()){
      String psSelect = null;
      if (snapshot.isPresent()) {
        psSelect = String.format("SELECT * FROM %s where entity_type = ? and entity_id = ? and event_id > ? order by event_id asc", eventTable);
      } else {
        psSelect = String.format("SELECT * FROM %s where entity_type = ? and entity_id = ? order by event_id asc", eventTable);
      }
      try (PreparedStatement stmt = connection.prepareStatement(psSelect)) {
        stmt.setString(1, aggregateType);
        stmt.setString(2, entityId);
        if (snapshot.isPresent()) {
          stmt.setString(3, snapshot.get().getSerializedSnapshot().getEntityVersion().asString());
        }
        try (ResultSet rs = stmt.executeQuery()) {
          while (rs.next()) {
            String eventId = rs.getString("event_id");
            String eventType = rs.getString("event_type");
            String eventData = rs.getString("event_data");
            String triggeringEvent = rs.getString("triggering_event");
            Optional<String> metadata = Optional.ofNullable(rs.getString("metadata"));
            EventIdTypeAndData eventIdTypeAndData = new EventIdTypeAndData(Int128.fromString(eventId), eventType, eventData, metadata);
            events.add(new EventAndTrigger(eventIdTypeAndData, triggeringEvent));
          }
        }
      }
    } catch (SQLException e) {
      logger.error("SqlException:", e);
    }
    logger.debug("Loaded {} events", events);
    Optional<EventAndTrigger> matching = findOptions.
            flatMap(AggregateCrudFindOptions::getTriggeringEvent).
            flatMap(te -> events.stream().filter(e -> te.getEventToken().equals(e.triggeringEvent)).findAny());
    if (matching.isPresent()) {
      throw(new DuplicateTriggeringEventException());
    }
    if (!snapshot.isPresent() && events.isEmpty())
      throw(new EntityNotFoundException());
    else {
      return new LoadedEvents(snapshot.map(LoadedSnapshot::getSerializedSnapshot), events.stream().map(e -> e.event).collect(Collectors.toList()));

    }
  }


  @Override
  public SaveUpdateResult update(EntityIdAndType entityIdAndType, Int128 entityVersion, List<EventTypeAndData> events, Optional<AggregateCrudUpdateOptions> updateOptions) {

    // TODO - triggering event check

    List<EventIdTypeAndData> eventsWithIds = events.stream().map(this::toEventWithId).collect(Collectors.toList());

    String aggregateType = entityIdAndType.getEntityType();
    String entityId = entityIdAndType.getEntityId();

    Int128 updatedEntityVersion = last(eventsWithIds).getId();

    String update_entities = String.format("UPDATE %s SET entity_version = ? WHERE entity_type = ? and entity_id = ? and entity_version = ?", entityTable);
    String insert_events = String.format("INSERT INTO %s (event_id, event_type, event_data, entity_type, entity_id, triggering_event, metadata) VALUES (?, ?, ?, ?, ?, ?, ?)", eventTable);

    try (final Connection connection = dataSource.getConnection()) {
      connection.setAutoCommit(false);
      try (PreparedStatement psEntity = connection.prepareStatement(update_entities)) {
        psEntity.setString(1, updatedEntityVersion.asString());
        psEntity.setString(2, aggregateType);
        psEntity.setString(3, entityId);
        psEntity.setString(4, entityVersion.asString());
        int count = psEntity.executeUpdate();
        if (count != 1) {
          logger.error("Failed to update entity: {}", count);
          throw(new OptimisticLockingException(entityIdAndType, entityVersion));
        }
      }

      updateOptions.flatMap(AggregateCrudUpdateOptions::getSnapshot).ifPresent(ss -> {
        Optional<LoadedSnapshot> previousSnapshot = getSnapshot(aggregateType, entityId);

        List<EventAndTrigger> oldEvents = new ArrayList<>();
        String psSelect = null;
        if (previousSnapshot.isPresent()) {
          psSelect = String.format("SELECT * FROM %s where entity_type = ? and entity_id = ? and and event_id > ? order by event_id asc", eventTable);
        } else {
          psSelect = String.format("SELECT * FROM %s where entity_type = ? and entity_id = ? order by event_id asc", eventTable);
        }
        try (PreparedStatement stmt = connection.prepareStatement(psSelect)) {
          stmt.setString(1, aggregateType);
          stmt.setString(2, entityId);
          if (previousSnapshot.isPresent()) {
            stmt.setString(3, previousSnapshot.get().getSerializedSnapshot().getEntityVersion().asString());
          }
          try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
              String eventId = rs.getString("event_id");
              String eventType = rs.getString("event_type");
              String eventData = rs.getString("event_data");
              String triggeringEvent = rs.getString("triggering_event");
              Optional<String> metadata = Optional.ofNullable(rs.getString("metadata"));
              EventIdTypeAndData eventIdTypeAndData = new EventIdTypeAndData(Int128.fromString(eventId), eventType, eventData, metadata);
              oldEvents.add(new EventAndTrigger(eventIdTypeAndData, triggeringEvent));
            }
          }

          String triggeringEvents = snapshotTriggeringEvents(previousSnapshot, oldEvents, updateOptions.flatMap(AggregateCrudUpdateOptions::getTriggeringEvent));

          String insert_snapshot = String.format("INSERT INTO %s (entity_type, entity_id, entity_version, snapshot_type, snapshot_json, triggering_events) VALUES (?, ?, ?, ?, ?, ?)", snapshotTable);
          try (PreparedStatement psEntity = connection.prepareStatement(insert_snapshot)) {
            psEntity.setString(1, aggregateType);
            psEntity.setString(2, entityId);
            psEntity.setString(3, updatedEntityVersion.asString());
            psEntity.setString(4, ss.getSnapshotType());
            psEntity.setString(5, ss.getJson());
            psEntity.setString(6, triggeringEvents);
            int count = psEntity.executeUpdate();
            if (count != 1) {
              logger.error("Failed to update entity: {}", count);
            }
          }
        }catch (SQLException e) {
          logger.error("SqlException:", e);
        }

      });



      try (PreparedStatement psEvent = connection.prepareStatement(insert_events)) {
        for(EventIdTypeAndData event : eventsWithIds) {
          psEvent.setString(1, event.getId().asString());
          psEvent.setString(2, event.getEventType());
          psEvent.setString(3, event.getEventData());
          psEvent.setString(4, aggregateType);
          psEvent.setString(5, entityId);
          psEvent.setString(6, updateOptions.flatMap(AggregateCrudUpdateOptions::getTriggeringEvent).map(EventContext::getEventToken).orElse(null));
          psEvent.setString(7, event.getMetadata().orElse(null));
          psEvent.addBatch();
        }
        psEvent.executeBatch();
      }
      connection.commit();
    } catch (OptimisticLockingException e) {
      throw e;
    } catch (SQLException e) {
      // the rollback is called automatically by the HikariCP
      logger.error("SqlException:", e);
    }

    return new SaveUpdateResult(new EntityIdVersionAndEventIds(entityId,
            updatedEntityVersion,
            eventsWithIds.stream().map(EventIdTypeAndData::getId).collect(Collectors.toList())),
            new PublishableEvents(aggregateType, entityId, eventsWithIds));
  }


  protected void checkSnapshotForDuplicateEvent(LoadedSnapshot ss, EventContext te) {
    // do nothing
  }

  protected String snapshotTriggeringEvents(Optional<LoadedSnapshot> previousSnapshot, List<EventAndTrigger> events, Optional<EventContext> eventContext) {
    // do nothing
    return null;
  }
}
