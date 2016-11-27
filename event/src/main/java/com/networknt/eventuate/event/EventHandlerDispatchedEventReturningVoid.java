package com.networknt.eventuate.event;

import com.networknt.eventuate.common.DispatchedEvent;
import com.networknt.eventuate.common.Event;

import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;

class EventHandlerDispatchedEventReturningVoid implements EventHandler {
  private final Method method;
  private final Object eventHandler;

  public EventHandlerDispatchedEventReturningVoid(Method method, Object eventHandler) {
    this.method = method;
    this.eventHandler = eventHandler;
  }

  @Override
  public String toString() {
    return "EventHandlerDispatchedEventReturningVoid{" +
            "method=" + method +
            ", eventHandler=" + eventHandler +
            '}';
  }

  @Override
  public Class<Event> getEventType() {
    return EventHandlerProcessorUtil.getEventClass(method);
  }

  @Override
  public CompletableFuture<?> dispatch(DispatchedEvent<Event> de) {
    CompletableFuture<Void> cf = new CompletableFuture<>();
    try {
      method.invoke(eventHandler, de);
      cf.complete(null);
    } catch (Throwable e) {
      cf.completeExceptionally(e);
    }
    return cf;
  }

}
