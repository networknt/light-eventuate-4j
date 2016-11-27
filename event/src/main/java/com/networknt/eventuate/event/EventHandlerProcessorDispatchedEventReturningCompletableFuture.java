package com.networknt.eventuate.event;

import com.networknt.eventuate.common.DispatchedEvent;

import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;

public class EventHandlerProcessorDispatchedEventReturningCompletableFuture implements EventHandlerProcessor {

  @Override
  public boolean supports(Method method) {
    return EventHandlerProcessorUtil.isMethodWithOneParameterOfTypeReturning(method, DispatchedEvent.class, CompletableFuture.class);
  }

  @Override
  public EventHandler process(Object eventHandler, Method method) {
    return new EventHandlerDispatchedEventReturningCompletableFuture(method, eventHandler);
  }


}
