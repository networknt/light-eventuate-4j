package com.networknt.eventuate.common;

import java.util.List;
import java.util.Optional;

public interface SnapshotManager {
  Optional<Snapshot> possiblySnapshot(Aggregate aggregate,  List<Event> oldEvents, List<Event> newEvents);
  Aggregate recreateFromSnapshot(Class<?> clasz, Snapshot snapshot);
}
