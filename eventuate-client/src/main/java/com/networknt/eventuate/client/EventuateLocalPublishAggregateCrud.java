package com.networknt.eventuate.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.eventuate.server.common.PublishedEvent;
import com.networknt.eventuate.common.EntityIdAndType;
import com.networknt.eventuate.common.EventContext;
import com.networknt.eventuate.common.impl.EventIdTypeAndData;
import com.networknt.eventuate.common.impl.SerializedEvent;
import com.networknt.eventuate.jdbc.AbstractEventuateJdbcAggregateStore;
import com.networknt.eventuate.kafka.producer.EventuateKafkaProducer;


import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * general command side AggregateCrud for save the Aggregate and events to event store
 *  the event store will be database which doesn't support cdc service.
 *  event will be published to Kafka by Kafka producer
 *  * @param dataSource DB data source
 */
public class EventuateLocalPublishAggregateCrud extends AbstractEventuateJdbcAggregateStore
{

  private AtomicLong eventOffset = new AtomicLong();
  private final Map<EntityIdAndType, List<SerializedEvent>> localEventsMap = new HashMap<EntityIdAndType, List<SerializedEvent>>();
  private EventuateKafkaProducer producer;

  public EventuateLocalPublishAggregateCrud(DataSource dataSource) {
    super(dataSource);
    producer = new EventuateKafkaProducer();

  }

  protected void publish(String aggregateType, String aggregateId, List<EventIdTypeAndData> eventsWithIds) {
    EntityIdAndType entityIdAndType = new EntityIdAndType(aggregateId, aggregateType);
    localEventsMap.put(entityIdAndType, eventsWithIds.stream().map(item->toSerializedEvent(item, aggregateType, aggregateId)).collect(Collectors.toList()));

    eventsWithIds.stream().map(item-> producer.send(aggregateType, aggregateId, toJson(toPublishedEvent(aggregateType, aggregateId, item))));
   }

  private SerializedEvent toSerializedEvent(EventIdTypeAndData event, String aggregateType, String aggregateId) {
    return new SerializedEvent(event.getId(), aggregateId, aggregateType, event.getEventData(), event.getEventType(),
            aggregateId.hashCode() % 8,
            eventOffset.getAndIncrement(),
            new EventContext(event.getId().asString()), event.getMetadata());
  }

  private String toJson(PublishedEvent eventInfo) {
    ObjectMapper om = new ObjectMapper();
    try {
      return om.writeValueAsString(eventInfo);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private PublishedEvent toPublishedEvent( String aggregateType, String aggregateId, EventIdTypeAndData event) {
    return new PublishedEvent(event.getId().toString(), aggregateId, aggregateType, event.getEventData(), event.getEventType(), null, event.getMetadata());
  }
}
