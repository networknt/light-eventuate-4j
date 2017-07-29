package com.networknt.eventuate.common;

import java.util.Arrays;
import java.util.List;

/**
 * General utility methods for event
 *
 */
public class EventUtil {

  public static List<Event> events(Event... events) {
    return Arrays.asList(events);
  }
}
