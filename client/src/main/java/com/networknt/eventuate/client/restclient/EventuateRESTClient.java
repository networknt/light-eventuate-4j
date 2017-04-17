package com.networknt.eventuate.client.restclient;

import com.networknt.eventuate.common.*;
import com.networknt.eventuate.common.impl.EntityIdVersionAndEventIds;
import com.networknt.eventuate.common.impl.EventTypeAndData;
import com.networknt.eventuate.common.impl.LoadedEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import com.networknt.client.Client;
import com.networknt.eventuate.common.impl.AggregateCrud;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 *  RestFul Client used for process Event Sourcing Aggregate Crud process
 * Created by gavin on 2017-04-16.
 */
public class EventuateRESTClient implements AggregateCrud{

    protected Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public CompletableFuture<EntityIdVersionAndEventIds> save(String aggregateType, List<EventTypeAndData> events, Optional<SaveOptions> options) {
        return null;
    }

    @Override
    public <T extends Aggregate<T>> CompletableFuture<LoadedEvents> find(String aggregateType, String entityId, Optional<FindOptions> findOptions) {
        return null;
    }

    @Override
    public CompletableFuture<EntityIdVersionAndEventIds> update(EntityIdAndType entityIdAndType, Int128 entityVersion, List<EventTypeAndData> events, Optional<UpdateOptions> updateOptions) {
        return null;
    }
}
