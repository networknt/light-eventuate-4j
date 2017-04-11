package com.networknt.eventuate.common;

import java.util.List;
import java.util.Optional;

/**
 * A strategy for creating snapshots of a
 */
public interface SnapshotStrategy {

  /**
   * The aggregate class that this is a strategy for
   *
   * @return the aggregate class
   */
  Class<?> getAggregateClass();

  /**
   * Possibly generate a snapshot
   *
   * @param aggregate - the updated aggregate
   * @param oldEvents - the old events that were used to recreate the aggregate
   * @param newEvents - the new events generated as a result of executing a command
   * @return an optional snapshot
   */
  Optional<Snapshot> possiblySnapshot(Aggregate aggregate, List<Event> oldEvents, List<Event> newEvents);

  /**
   * Recreate an aggregate from a snapshot
   *
   * @param clasz the aggregate class
   * @param snapshot the snapshot
   * @return the aggregate
   */
  Aggregate recreateAggregate(Class<?> clasz, Snapshot snapshot);
}
