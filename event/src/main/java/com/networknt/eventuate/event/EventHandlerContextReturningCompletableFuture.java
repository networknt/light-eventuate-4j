package com.networknt.eventuate.event;

import com.networknt.eventuate.common.CompletableFutureUtil;
import com.networknt.eventuate.common.DispatchedEvent;
import com.networknt.eventuate.common.Event;
import com.networknt.eventuate.common.EventuateAggregateStore;

import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;

class EventHandlerContextReturningCompletableFuture implements EventHandler {
  private final Method method;
  private final Object eventHandler;
  private EventuateAggregateStore aggregateStore;

  public EventHandlerContextReturningCompletableFuture(EventuateAggregateStore aggregateStore, Method method, Object eventHandler) {
    this.aggregateStore = aggregateStore;
    this.method = method;
    this.eventHandler = eventHandler;
  }

  @Override
  public String toString() {
    return "EventHandlerContextReturningCompletableFuture{" +
            "method=" + method +
            ", eventHandler=" + eventHandler +
            ", aggregateStore=" + aggregateStore +
            '}';
  }

  @Override
  public Class<Event> getEventType() {
    return EventHandlerProcessorUtil.getEventClass(method);
  }

  @Override
  public CompletableFuture<?> dispatch(DispatchedEvent<Event> de) {
    try {
      return (CompletableFuture<?>) method.invoke(eventHandler, new EventHandlerContextImpl(aggregateStore, de));
    } catch (Throwable e) {
      return CompletableFutureUtil.failedFuture(e);
    }
  }
}
