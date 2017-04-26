package com.networknt.eventuate.jdbc;

import com.networknt.eventuate.common.*;
import com.networknt.eventuate.common.impl.*;
import com.networknt.eventuate.common.impl.sync.AggregateCrud;
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

public abstract class AbstractEventuateJdbcAggregateStore implements AggregateCrud {

  protected Logger logger = LoggerFactory.getLogger(getClass());

  private DataSource dataSource;

  public AbstractEventuateJdbcAggregateStore(DataSource dataSource) {
    this.dataSource = dataSource;
  }


  private IdGenerator idGenerator = new IdGeneratorImpl();

  @Override
  public EntityIdVersionAndEventIds save(String aggregateType, List<EventTypeAndData> events, Optional<SaveOptions> saveOptions) {
    List<EventIdTypeAndData> eventsWithIds = events.stream().map(this::toEventWithId).collect(Collectors.toList());
    String entityId = saveOptions.flatMap(SaveOptions::getEntityId).orElse(idGenerator.genId().asString());

    Int128 entityVersion = last(eventsWithIds).getId();

    String insert_entities = "INSERT INTO entities (entity_type, entity_id, entity_version) VALUES (?, ?, ?)";
    String insert_events = "INSERT INTO events (event_id, event_type, event_data, entity_type, entity_id, triggering_event) VALUES (?, ?, ?, ?, ?, ?)";

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
          psEvent.setString(6, saveOptions.flatMap(SaveOptions::getTriggeringEvent).map(EventContext::getEventToken).orElse(null));
          psEvent.addBatch();
        }
        psEvent.executeBatch();
      }
      connection.commit();
    } catch (SQLException e) {
      // the rollback is called automatically by the HikariCP
      logger.error("SqlException:", e);
    }
    publish(aggregateType, entityId, eventsWithIds);
    return new EntityIdVersionAndEventIds(entityId, entityVersion, eventsWithIds.stream().map(EventIdTypeAndData::getId).collect(Collectors.toList()));
  }


  private <T> T last(List<T> eventsWithIds) {
    return eventsWithIds.get(eventsWithIds.size() - 1);
  }

  private EventIdTypeAndData toEventWithId(EventTypeAndData eventTypeAndData) {
    return new EventIdTypeAndData(idGenerator.genId(), eventTypeAndData.getEventType(), eventTypeAndData.getEventData());
  }

  class EventAndTrigger {

    public final EventIdTypeAndData event;
    public final String triggeringEvent;

    public EventAndTrigger(EventIdTypeAndData event, String triggeringEvent) {

      this.event = event;
      this.triggeringEvent = triggeringEvent;
    }
  }

  @Override
  public <T extends Aggregate<T>> LoadedEvents find(String aggregateType, String entityId, Optional<FindOptions> findOptions) {
    Optional<LoadedSnapshot> snapshot = getSnapshot(aggregateType, entityId);
    List<EventAndTrigger> events = new ArrayList<>();
    try (final Connection connection = dataSource.getConnection()){
      String psSelect = null;
      if (snapshot.isPresent()) {
        psSelect = "SELECT * FROM events where entity_type = ? and entity_id = ? and and event_id > ? order by event_id asc";
      } else {
        psSelect = "SELECT * FROM events where entity_type = ? and entity_id = ? order by event_id asc";
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
            EventIdTypeAndData eventIdTypeAndData = new EventIdTypeAndData(Int128.fromString(eventId), eventType, eventData);
            events.add(new EventAndTrigger(eventIdTypeAndData, triggeringEvent));
          }
        }
      }
    } catch (SQLException e) {
      logger.error("SqlException:", e);
    }
    logger.debug("Loaded {} events", events);
    Optional<EventAndTrigger> matching = findOptions.
            flatMap(FindOptions::getTriggeringEvent).
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

  private Optional<LoadedSnapshot> getSnapshot(String aggregateType, String entityId) {
    LoadedSnapshot snapshot = null;
    try (final Connection connection = dataSource.getConnection()){
      String psSelect = "select snapshot_type, snapshot_json, entity_version, triggering_Events from snapshots where entity_type = ? and entity_id = ? order by entity_version desc LIMIT 1";
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
  public EntityIdVersionAndEventIds update(EntityIdAndType entityIdAndType, Int128 entityVersion, List<EventTypeAndData> events, Optional<UpdateOptions> updateOptions) {
    List<EventIdTypeAndData> eventsWithIds = events.stream().map(this::toEventWithId).collect(Collectors.toList());

    String entityType = entityIdAndType.getEntityType();
    String entityId = entityIdAndType.getEntityId();

    Int128 updatedEntityVersion = last(eventsWithIds).getId();

    String update_entities = "UPDATE entities SET entity_version = ? WHERE entity_type = ? and entity_id = ? and entity_version = ?";
    String insert_events = "INSERT INTO events (event_id, event_type, event_data, entity_type, entity_id, triggering_event) VALUES (?, ?, ?, ?, ?, ?)";

    try (final Connection connection = dataSource.getConnection()) {
      connection.setAutoCommit(false);
      try (PreparedStatement psEntity = connection.prepareStatement(update_entities)) {
        psEntity.setString(1, updatedEntityVersion.asString());
        psEntity.setString(2, entityType);
        psEntity.setString(3, entityId);
        psEntity.setString(4, entityVersion.asString());
        int count = psEntity.executeUpdate();
        if (count != 1) {
          logger.error("Failed to update entity: {}", count);
          throw(new OptimisticLockingException(entityIdAndType, entityVersion));
        }
      }

      updateOptions.flatMap(UpdateOptions::getSnapshot).ifPresent(ss -> {
        Optional<LoadedSnapshot> previousSnapshot = getSnapshot(entityType, entityId);

        List<EventAndTrigger> oldEvents = null;
        String psSelect = null;
        if (previousSnapshot.isPresent()) {
          psSelect = "SELECT * FROM events where entity_type = ? and entity_id = ? and and event_id > ? order by event_id asc";
        } else {
          psSelect = "SELECT * FROM events where entity_type = ? and entity_id = ? order by event_id asc";
        }
        try (PreparedStatement stmt = connection.prepareStatement(psSelect)) {
          stmt.setString(1, entityType);
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
              EventIdTypeAndData eventIdTypeAndData = new EventIdTypeAndData(Int128.fromString(eventId), eventType, eventData);
              oldEvents.add(new EventAndTrigger(eventIdTypeAndData, triggeringEvent));
            }
          }

        String triggeringEvents = snapshotTriggeringEvents(previousSnapshot, oldEvents, updateOptions.flatMap(UpdateOptions::getTriggeringEvent));

        String insert_snapshot = "INSERT INTO snapshots (entity_type, entity_id, entity_version, snapshot_type, snapshot_json, triggering_events) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement psEntity = connection.prepareStatement(insert_snapshot)) {
          psEntity.setString(1, entityType);
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
          psEvent.setString(4, entityType);
          psEvent.setString(5, entityId);
          psEvent.setString(6, updateOptions.flatMap(UpdateOptions::getTriggeringEvent).map(EventContext::getEventToken).orElse(null));
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

    publish(entityIdAndType.getEntityType(), entityId, eventsWithIds);

    return new EntityIdVersionAndEventIds(entityId,
            updatedEntityVersion,
            eventsWithIds.stream().map(EventIdTypeAndData::getId).collect(Collectors.toList()));

  }

  protected abstract void publish(String aggregateType, String aggregateId, List<EventIdTypeAndData> eventsWithIds);

  protected void checkSnapshotForDuplicateEvent(LoadedSnapshot ss, EventContext te) {
    // TODO
  }

  protected String snapshotTriggeringEvents(Optional<LoadedSnapshot> previousSnapshot, List<EventAndTrigger> events, Optional<EventContext> eventContext) {
    //TODO
    return null;
  }


}
