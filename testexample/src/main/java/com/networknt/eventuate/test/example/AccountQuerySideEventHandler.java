package com.networknt.eventuate.test.example;

import com.networknt.eventuate.common.DispatchedEvent;
import com.networknt.eventuate.common.EventHandlerMethod;
import com.networknt.eventuate.common.EventSubscriber;
import com.networknt.eventuate.test.domain.AccountCreatedEvent;

@EventSubscriber(id="javaIntegrationTestQuerySideAccountEventHandlers")
public class AccountQuerySideEventHandler {

  private EventTracker<DispatchedEvent<AccountCreatedEvent>> events = EventTracker.create();

  public EventTracker<DispatchedEvent<AccountCreatedEvent>> getEvents() {
    return events;
  }

  @EventHandlerMethod
  public void create(DispatchedEvent<AccountCreatedEvent> de) {
    events.onNext(de);
  }
}
