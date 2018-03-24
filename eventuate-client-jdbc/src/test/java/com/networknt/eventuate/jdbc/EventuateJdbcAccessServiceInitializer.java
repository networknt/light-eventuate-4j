package com.networknt.eventuate.jdbc;

import com.networknt.eventuate.common.impl.AggregateCrud;
import com.networknt.eventuate.common.impl.AggregateEvents;
import com.networknt.eventuate.common.impl.adapters.SyncToAsyncAggregateCrudAdapter;
import com.networknt.eventuate.common.impl.adapters.SyncToAsyncAggregateEventsAdapter;
import com.networknt.eventuate.jdbc.common.EventuateJdbcAccess;
import com.networknt.eventuate.jdbc.common.EventuateSchema;
import com.networknt.service.SingletonServiceFactory;

public class EventuateJdbcAccessServiceInitializer {

    public EventuateSchema eventuateSchema() {
        return new EventuateSchema();
    }

    public AggregateCrud aggregateCrud() {
        com.networknt.eventuate.common.impl.sync.AggregateCrud aggregateCrud = SingletonServiceFactory.getBean(com.networknt.eventuate.common.impl.sync.AggregateCrud.class);
        return new SyncToAsyncAggregateCrudAdapter(aggregateCrud);
    }

    public AggregateEvents aggregateEvents() {
        com.networknt.eventuate.common.impl.sync.AggregateEvents aggregateEvents = SingletonServiceFactory.getBean(com.networknt.eventuate.common.impl.sync.AggregateEvents.class);
        return new SyncToAsyncAggregateEventsAdapter(aggregateEvents);
    }

    public EventuateEmbeddedTestAggregateStore eventuateEmbeddedTestAggregateStore() {
        EventuateJdbcAccess eventuateJdbcAccess = SingletonServiceFactory.getBean(EventuateJdbcAccess.class);
        return new EventuateEmbeddedTestAggregateStore(eventuateJdbcAccess);
    }
}
