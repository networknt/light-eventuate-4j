package com.networknt.eventuate.event;

import java.lang.reflect.AccessibleObject;


public interface EventHandlerProcessor {
  boolean supports(AccessibleObject method);

  EventHandler process(Object eventHandler, AccessibleObject method);
}
