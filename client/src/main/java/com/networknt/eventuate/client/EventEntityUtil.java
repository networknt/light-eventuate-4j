package com.networknt.eventuate.client;

import com.networknt.eventuate.common.Event;
import com.networknt.eventuate.common.EventEntity;

/**
 * General utility methods for working with Event Entity conversion
 *
 */
public class EventEntityUtil {
  public static Class<?> toEntityType(Class<Event> eventType) {
    String entityName = toEntityTypeName(eventType);
    try {
      return Class.forName(entityName);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }

  }

  public static String toEntityTypeName(Class<Event> eventType) {
    EventEntity a = AnnotationUtils.findAnnotation(eventType, EventEntity.class);
    if (a == null)
      a = eventType.getPackage().getAnnotation(EventEntity.class);
    if (a == null)
      throw new RuntimeException("Neither this event class " + eventType.getName() + " nor it's package has a EventEntity annotation");

    return a.entity();
  }
}
