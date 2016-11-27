package com.networknt.eventuate.event;

import com.networknt.eventuate.common.DispatchedEvent;

import java.lang.reflect.Method;

public class EventHandlerProcessorDispatchedEventReturningVoid implements EventHandlerProcessor {

  @Override
  public boolean supports(Method method) {
    return EventHandlerProcessorUtil.isVoidMethodWithOneParameterOfType(method, DispatchedEvent.class);
  }

  @Override
  public EventHandler process(Object eventHandler, Method method) {
    return new EventHandlerDispatchedEventReturningVoid(method, eventHandler);
  }


}
