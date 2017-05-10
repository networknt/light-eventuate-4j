package com.networknt.eventuate.event;

import com.networknt.eventuate.common.DispatchedEvent;
import com.networknt.eventuate.common.Event;

import java.util.concurrent.CompletableFuture;

/**
 * interface to indicator Event Handler
 *
 */
public interface EventHandler {

  /**
   * get event type which handled by the event handler
    * @return Class the class of the event
   */
  Class<Event> getEventType();

  /**
   * get event type which handled by the event handler
   * @return Class the class of the event
   */
  CompletableFuture<?> dispatch(DispatchedEvent<Event> de);
}
