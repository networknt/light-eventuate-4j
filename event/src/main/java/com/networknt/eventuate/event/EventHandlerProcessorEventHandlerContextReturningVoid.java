package com.networknt.eventuate.event;

import com.networknt.eventuate.common.EventHandlerContext;
import com.networknt.eventuate.common.EventuateAggregateStore;

import java.lang.reflect.Method;

public class EventHandlerProcessorEventHandlerContextReturningVoid implements EventHandlerProcessor {

  private EventuateAggregateStore aggregateStore;

  public EventHandlerProcessorEventHandlerContextReturningVoid(EventuateAggregateStore aggregateStore) {
    this.aggregateStore = aggregateStore;
  }

  @Override
  public boolean supports(Method method) {
    return EventHandlerProcessorUtil.isVoidMethodWithOneParameterOfType(method, EventHandlerContext.class);
  }

  @Override
  public EventHandler process(Object eventHandler, Method method) {
    return new EventHandlerEventHandlerContextReturningVoid(aggregateStore, method, eventHandler);
  }


}
