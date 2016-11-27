package com.networknt.eventuate.event;

import java.lang.reflect.Method;

public interface EventHandlerProcessor {
  boolean supports(Method method);

  EventHandler process(Object eventHandler, Method method);
}
