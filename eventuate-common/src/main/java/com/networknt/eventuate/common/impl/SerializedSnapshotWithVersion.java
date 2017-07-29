package com.networknt.eventuate.common.impl;

import com.networknt.eventuate.common.Int128;

public class SerializedSnapshotWithVersion {

  private SerializedSnapshot serializedSnapshot;
  private Int128 entityVersion;

  public SerializedSnapshotWithVersion(SerializedSnapshot serializedSnapshot, Int128 entityVersion) {
    this.serializedSnapshot = serializedSnapshot;
    this.entityVersion = entityVersion;
  }

  public SerializedSnapshot getSerializedSnapshot() {
    return serializedSnapshot;
  }

  public Int128 getEntityVersion() {
    return entityVersion;
  }
}
