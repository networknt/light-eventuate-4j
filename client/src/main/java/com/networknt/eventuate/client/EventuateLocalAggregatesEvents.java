package com.networknt.eventuate.client;


import com.networknt.eventuate.common.SubscriberOptions;
import com.networknt.eventuate.common.impl.SerializedEvent;
import com.networknt.eventuate.common.impl.sync.AggregateEvents;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * general command side AggregateEvents for audit purpose
 *  In some cases, command side just publish events out and won't do subscribe event process
 *
 *
 */
public class EventuateLocalAggregatesEvents implements AggregateEvents {

    private final Map<String, List<Subscription>> aggregateTypeToSubscription = new HashMap<>();
    class Subscription {

        private final String subscriberId;
        private final Map<String, Set<String>> aggregatesAndEvents;
        private final Function<SerializedEvent, CompletableFuture<?>> handler;

        public Subscription(String subscriberId, Map<String, Set<String>> aggregatesAndEvents, Function<SerializedEvent, CompletableFuture<?>> handler) {

            this.subscriberId = subscriberId;
            this.aggregatesAndEvents = aggregatesAndEvents;
            this.handler = handler;
        }

        public boolean isInterestedIn(String aggregateType, String eventType) {
            return aggregatesAndEvents.get(aggregateType) != null && aggregatesAndEvents.get(aggregateType).contains(eventType);
        }
    }

    @Override
    public void subscribe(String subscriberId, Map<String, Set<String>> aggregatesAndEvents, SubscriberOptions options, Function<SerializedEvent, CompletableFuture<?>> handler) {
        // TODO handle options
        Subscription subscription = new Subscription(subscriberId, aggregatesAndEvents, handler);
        synchronized (aggregateTypeToSubscription) {
            for (String aggregateType : aggregatesAndEvents.keySet()) {
                List<Subscription> existing = aggregateTypeToSubscription.get(aggregateType);
                if (existing == null) {
                    existing = new LinkedList<>();
                    aggregateTypeToSubscription.put(aggregateType, existing);
                }
                existing.add(subscription);
            }
        }
    }


}
