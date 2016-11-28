package com.networknt.eventuate.jdbc;

import com.networknt.eventuate.common.*;
import com.networknt.eventuate.common.impl.sync.AggregateCrud;
import com.networknt.eventuate.common.impl.EntityIdVersionAndEventIds;
import com.networknt.eventuate.common.impl.EventIdTypeAndData;
import com.networknt.eventuate.common.impl.EventTypeAndData;
import com.networknt.eventuate.common.impl.LoadedEvents;
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
    List<EventAndTrigger> events = new ArrayList<>();
    try (final Connection connection = dataSource.getConnection()){
      String psSelect = "SELECT * FROM events where entity_type = ? and entity_id = ? order by event_id asc";
      try (PreparedStatement stmt = connection.prepareStatement(psSelect);
           ResultSet rs = stmt.executeQuery()) {

        while (rs.next()) {
          String eventId = rs.getString("event_id");
          String eventType = rs.getString("event_type");
          String eventData = rs.getString("event_data");
          String triggeringEvent = rs.getString("triggering_event");
          EventIdTypeAndData eventIdTypeAndData = new EventIdTypeAndData(Int128.fromString(eventId), eventType, eventData);
          events.add(new EventAndTrigger(eventIdTypeAndData, triggeringEvent));
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
    if (events.isEmpty())
      throw(new EntityNotFoundException());
    else
      return new LoadedEvents(events.stream().map(e -> e.event).collect(Collectors.toList()));
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


}
