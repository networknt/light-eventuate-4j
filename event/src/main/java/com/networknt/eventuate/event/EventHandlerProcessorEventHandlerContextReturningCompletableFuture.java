package com.networknt.eventuate.event;

import com.networknt.eventuate.common.EventHandlerContext;
import com.networknt.eventuate.common.EventuateAggregateStore;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;

public class EventHandlerProcessorEventHandlerContextReturningCompletableFuture implements EventHandlerProcessor {

  private EventuateAggregateStore aggregateStore;

  public EventHandlerProcessorEventHandlerContextReturningCompletableFuture(EventuateAggregateStore aggregateStore) {
    this.aggregateStore = aggregateStore;
  }


  public boolean supportsMethod(Method method) {
    return EventHandlerProcessorUtil.isMethodWithOneParameterOfTypeReturning(method, EventHandlerContext.class, CompletableFuture.class);
  }


  public EventHandler processMethod(Object eventHandler, Method method) {
    return new EventHandlerContextReturningCompletableFuture(aggregateStore, method, eventHandler);
  }

  @Override
  public boolean supports(AccessibleObject fieldOrMethod) {
    return (fieldOrMethod instanceof Method) && supportsMethod((Method)fieldOrMethod);
  }


  @Override
  public EventHandler process(Object eventHandler, AccessibleObject fieldOrMethod) {
    return processMethod(eventHandler, (Method)fieldOrMethod);
  }

}
