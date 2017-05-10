package com.networknt.eventuate.event;

import java.lang.reflect.AccessibleObject;


/**
 * Base interface for an EventHandler that uses event sourcing
 *
 */
public interface EventHandlerProcessor {
  /**
   * the method used to verify if the eventhandle should be processed by the EventHandlerProcessor
   * @param method the event handler method
   * @return ture of false; if true, then the call process method to process the EventHandler
   */
  boolean supports(AccessibleObject method);

  /**
   * Process EventHandler
   * @param eventHandler the event handler object
   * @param method the event handler method
   * @return EventHandler
   */

  EventHandler process(Object eventHandler, AccessibleObject method);
}
