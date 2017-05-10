package com.networknt.eventuate.common;

import java.util.List;

/**
 * General utility methods for Aggregate
 *
 */
public class Aggregates {
  public static <T extends Aggregate<T>> T applyEventsToMutableAggregate(T aggregate, List<Event> events) {
    for (Event event : events) {
      aggregate = aggregate.applyEvent(event);
    }
    return aggregate;
  }

  public static <T extends Aggregate<T>> T recreateAggregate(Class<T> clasz, List<Event> events) {
    try {
      return applyEventsToMutableAggregate(clasz.newInstance(), events);
    } catch (InstantiationException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }
}
