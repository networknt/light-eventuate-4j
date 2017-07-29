package com.networknt.eventuate.common.impl;

public class SerializedSnapshot {

  private String snapshotType;
  private String json;

  public SerializedSnapshot(String snapshotType, String json) {
    this.snapshotType = snapshotType;
    this.json = json;
  }

  public String getSnapshotType() {
    return snapshotType;
  }

  public String getJson() {
    return json;
  }
}
