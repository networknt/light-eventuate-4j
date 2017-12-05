package com.networknt.eventuate.client;

public class EventuateClientServiceInitializer {
    public SubscriptionsRegistry subscriptionsRegistry() {
        return new SubscriptionsRegistry();
    }
}
