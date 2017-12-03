package com.networknt.eventuate.client;

import java.util.LinkedList;
import java.util.List;

public class SubscriptionsRegistry {

  public List<RegisteredSubscription> getRegisteredSubscriptions() {
    return registeredSubscriptions;
  }

  private List<RegisteredSubscription> registeredSubscriptions = new LinkedList<>();

  public void add(RegisteredSubscription registeredSubscription) {
    registeredSubscriptions.add(registeredSubscription);
  }


}
