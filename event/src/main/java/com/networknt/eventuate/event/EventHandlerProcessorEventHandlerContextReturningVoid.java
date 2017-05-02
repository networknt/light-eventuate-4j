package com.networknt.eventuate.event;

import com.networknt.eventuate.common.EventHandlerContext;
import com.networknt.eventuate.common.EventuateAggregateStore;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;

public class EventHandlerProcessorEventHandlerContextReturningVoid  implements EventHandlerProcessor {

  private EventuateAggregateStore aggregateStore;

  public EventHandlerProcessorEventHandlerContextReturningVoid(EventuateAggregateStore aggregateStore) {
    this.aggregateStore = aggregateStore;
  }


  public boolean supportsMethod(Method method) {
    return EventHandlerProcessorUtil.isVoidMethodWithOneParameterOfType(method, EventHandlerContext.class);
  }


  public EventHandler processMethod(Object eventHandler, Method method) {
    return new EventHandlerEventHandlerContextReturningVoid(aggregateStore, method, eventHandler);
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
