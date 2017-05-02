package com.networknt.eventuate.event;

import com.networknt.eventuate.common.DispatchedEvent;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;

public class EventHandlerProcessorDispatchedEventReturningCompletableFuture implements EventHandlerProcessor {

  public boolean supportsMethod(Method method) {
    return EventHandlerProcessorUtil.isMethodWithOneParameterOfTypeReturning(method, DispatchedEvent.class, CompletableFuture.class);
  }


  public EventHandler processMethod(Object eventHandler, Method method) {
    return new EventHandlerDispatchedEventReturningCompletableFuture(method, eventHandler);
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
