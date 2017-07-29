package com.networknt.eventuate.common;

public class EventuateSubscriptionFailedException extends EventuateException {
  public EventuateSubscriptionFailedException(String subscriberId, Exception e) {
    super("Subscription failed: " + subscriberId, e);
  }
}
