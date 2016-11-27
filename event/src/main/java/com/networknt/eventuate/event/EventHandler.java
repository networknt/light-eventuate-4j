package com.networknt.eventuate.event;

import com.networknt.eventuate.common.DispatchedEvent;
import com.networknt.eventuate.common.Event;

import java.util.concurrent.CompletableFuture;

public interface EventHandler {
  Class<Event> getEventType();

  CompletableFuture<?> dispatch(DispatchedEvent<Event> de);
}
