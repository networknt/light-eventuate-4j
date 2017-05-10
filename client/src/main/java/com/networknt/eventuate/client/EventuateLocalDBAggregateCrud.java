package com.networknt.eventuate.client;


import com.networknt.eventuate.common.EntityIdAndType;
import com.networknt.eventuate.common.EventContext;
import com.networknt.eventuate.common.impl.EventIdTypeAndData;
import com.networknt.eventuate.common.impl.SerializedEvent;
import com.networknt.eventuate.jdbc.AbstractEventuateJdbcAggregateStore;

import javax.sql.DataSource;
import java.util.*;

import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;


/**
 * general command side AggregateCrud for save the Aggregate and events to event store
 *  the event store will be database if application use this AggregateCrud
 *  event publish will relay on cdc service
 *  * @param dataSource DB data source
 */
public class EventuateLocalDBAggregateCrud extends AbstractEventuateJdbcAggregateStore
{

  private AtomicLong eventOffset = new AtomicLong();
  private final Map<EntityIdAndType, List<SerializedEvent>> localEventsMap = new HashMap<EntityIdAndType, List<SerializedEvent>>();

  public EventuateLocalDBAggregateCrud(DataSource dataSource) {
    super(dataSource);
  }

  protected void publish(String aggregateType, String aggregateId, List<EventIdTypeAndData> eventsWithIds) {
    EntityIdAndType entityIdAndType = new EntityIdAndType(aggregateId, aggregateType);
    localEventsMap.put(entityIdAndType, eventsWithIds.stream().map(item->toSerializedEvent(item, aggregateType, aggregateId)).collect(Collectors.toList()));
    // doing nothing here, The capture event data change will process by cdc service
  }

  private SerializedEvent toSerializedEvent(EventIdTypeAndData event, String aggregateType, String aggregateId) {
    return new SerializedEvent(event.getId(), aggregateId, aggregateType, event.getEventData(), event.getEventType(),
            aggregateId.hashCode() % 8,
            eventOffset.getAndIncrement(),
            new EventContext(event.getId().asString()));
  }


}
