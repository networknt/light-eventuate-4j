package com.networknt.eventuate.common.impl;

import java.util.List;
import java.util.Optional;

public class LoadedEvents {

  private List<EventIdTypeAndData> events;
  private Optional<SerializedSnapshotWithVersion> snapshot;

  public LoadedEvents(Optional<SerializedSnapshotWithVersion> snapshot, List<EventIdTypeAndData> events) {
    this.events = events;
    this.snapshot = snapshot;
  }

  public Optional<SerializedSnapshotWithVersion> getSnapshot() {
    return snapshot;
  }

  public List<EventIdTypeAndData> getEvents() {
    return events;
  }
}
