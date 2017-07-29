package com.networknt.eventuate.event;

import com.networknt.eventuate.common.EventHandlerContext;
import com.networknt.eventuate.common.EventuateAggregateStore;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;

public class EventHandlerProcessorEventHandlerContextReturningVoid extends EventHandlerMethodProcessor {

  private EventuateAggregateStore aggregateStore;

  public EventHandlerProcessorEventHandlerContextReturningVoid(EventuateAggregateStore aggregateStore) {
    this.aggregateStore = aggregateStore;
  }

  @Override
  public boolean supportsMethod(Method method) {
    return EventHandlerProcessorUtil.isVoidMethodWithOneParameterOfType(method, EventHandlerContext.class);
  }

  @Override
  public EventHandler processMethod(Object eventHandler, Method method) {
    return new EventHandlerEventHandlerContextReturningVoid(aggregateStore, method, eventHandler);
  }
}
