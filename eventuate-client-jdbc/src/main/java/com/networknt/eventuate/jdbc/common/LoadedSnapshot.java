package com.networknt.eventuate.jdbc.common;

import com.networknt.eventuate.common.impl.SerializedSnapshotWithVersion;

public class LoadedSnapshot  {
  private final SerializedSnapshotWithVersion serializedSnapshot;
  private String triggeringEvents;

  public LoadedSnapshot(SerializedSnapshotWithVersion serializedSnapshot, String triggeringEvents) {

    this.serializedSnapshot = serializedSnapshot;
    this.triggeringEvents = triggeringEvents;
  }

  public SerializedSnapshotWithVersion getSerializedSnapshot() {
    return serializedSnapshot;
  }

  public String getTriggeringEvents() {
    return triggeringEvents;
  }
}
