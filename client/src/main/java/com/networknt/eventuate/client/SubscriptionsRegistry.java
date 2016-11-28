package com.networknt.eventuate.client;

import java.util.List;

public interface SubscriptionsRegistry {

  List<RegisteredSubscription> getRegisteredSubscriptions();

  void add(RegisteredSubscription registeredSubscription);

}
