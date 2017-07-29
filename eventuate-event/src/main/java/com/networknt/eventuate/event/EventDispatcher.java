package com.networknt.eventuate.event;

import com.networknt.eventuate.common.CompletableFutureUtil;
import com.networknt.eventuate.common.DispatchedEvent;
import com.networknt.eventuate.common.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static com.networknt.eventuate.common.impl.EventuateActivity.activityLogger;

/**
 * EventDispatcher execute event dispatch method based on the input event
 */
public class EventDispatcher {

  private static Logger logger = LoggerFactory.getLogger(EventDispatcher.class);

  private final String subscriberId;
  private final Map<Class<?>, EventHandler> eventTypesAndHandlers;

  /**
   * Construct EventDispatcher
   * @param subscriberId defined topic subscriberId
   * @param eventTypesAndHandlers defined event handlers
   */
  public EventDispatcher(String subscriberId, Map<Class<?>, EventHandler> eventTypesAndHandlers) {
    this.subscriberId = subscriberId;
    this.eventTypesAndHandlers = eventTypesAndHandlers;
  }

  /**
   * Dispatch event to event handler
   * @param de dispatched event
   * @return CompletableFuture
   */
  public CompletableFuture<?> dispatch(DispatchedEvent<Event> de) {
    EventHandler eventHandler = eventTypesAndHandlers.get(de.getEventType());
    if (eventHandler != null) {
      if (activityLogger.isDebugEnabled()) {
        activityLogger.debug("Invoking event handler {} {} {}", subscriberId, de, eventHandler);
        return CompletableFutureUtil.tap(eventHandler.dispatch(de), (result, throwable) -> {
           if (throwable == null)
              activityLogger.debug("Invoked event handler {} {} {}", subscriberId, de, eventHandler);
           else
              activityLogger.debug(String.format("Event handler failed %s %s %s", subscriberId, de, eventHandler), throwable);
        });
      }
      else
        return eventHandler.dispatch(de);
    } else {
      RuntimeException ex = new RuntimeException("No handler for event - subscriberId: " + subscriberId + ", " + de.getEventType());
      logger.error("dispatching failure", ex);
      CompletableFuture completableFuture = new CompletableFuture();
      completableFuture.completeExceptionally(ex);
      return completableFuture;
    }
  }
}
