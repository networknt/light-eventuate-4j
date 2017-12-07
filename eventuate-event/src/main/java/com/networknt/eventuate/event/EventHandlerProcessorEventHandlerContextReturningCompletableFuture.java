package com.networknt.eventuate.event;

import com.networknt.eventuate.common.EventHandlerContext;
import com.networknt.eventuate.common.EventuateAggregateStore;

import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;

public class EventHandlerProcessorEventHandlerContextReturningCompletableFuture extends EventHandlerMethodProcessor {

  private EventuateAggregateStore aggregateStore;

  public EventHandlerProcessorEventHandlerContextReturningCompletableFuture(EventuateAggregateStore aggregateStore) {
    this.aggregateStore = aggregateStore;
  }

  @Override
  public boolean supportsMethod(Method method) {
    return EventHandlerProcessorUtil.isMethodWithOneParameterOfTypeReturning(method, EventHandlerContext.class, CompletableFuture.class);
  }

  @Override
  public EventHandler processMethod(Object eventHandler, Method method) {
    return new EventHandlerContextReturningCompletableFuture(aggregateStore, method, eventHandler);
  }
}
