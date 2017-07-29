package com.networknt.eventuate.event;

import com.networknt.eventuate.common.DispatchedEvent;
import com.networknt.eventuate.common.Event;
import com.networknt.eventuate.common.EventuateAggregateStore;

import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;

class EventHandlerEventHandlerContextReturningVoid implements EventHandler {
  private EventuateAggregateStore aggregateStore;
  private final Method method;
  private final Object eventHandler;

  public EventHandlerEventHandlerContextReturningVoid(EventuateAggregateStore aggregateStore, Method method, Object eventHandler) {
    this.aggregateStore = aggregateStore;
    this.method = method;
    this.eventHandler = eventHandler;
  }

  @Override
  public String toString() {
    return "EventHandlerEventHandlerContextReturningVoid{" +
            "aggregateStore=" + aggregateStore +
            ", method=" + method +
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
      method.invoke(eventHandler, new EventHandlerContextImpl(aggregateStore, de));
      cf.complete(null);
    } catch (Throwable e) {
      cf.completeExceptionally(e);
    }
    return cf;
  }
}
