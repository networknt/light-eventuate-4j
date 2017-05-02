package com.networknt.eventuate.event;

import com.networknt.eventuate.common.DispatchedEvent;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;

public class EventHandlerProcessorDispatchedEventReturningVoid implements EventHandlerProcessor {


  public boolean supportsMethod(Method method) {
    return EventHandlerProcessorUtil.isVoidMethodWithOneParameterOfType(method, DispatchedEvent.class);
  }


  public EventHandler processMethod(Object eventHandler, Method method) {
    return new EventHandlerDispatchedEventReturningVoid(method, eventHandler);
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
