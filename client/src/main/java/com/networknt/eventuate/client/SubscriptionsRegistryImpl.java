package com.networknt.eventuate.client;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by stevehu on 2016-11-27.
 */
public class SubscriptionsRegistryImpl implements SubscriptionsRegistry {

    private List<RegisteredSubscription> registeredSubscriptions = new LinkedList<>();

    @Override
    public List<RegisteredSubscription> getRegisteredSubscriptions() {
        return registeredSubscriptions;
    }

    @Override
    public void add(RegisteredSubscription registeredSubscription) {
        registeredSubscriptions.add(registeredSubscription);
    }

}
