package com.networknt.eventuate.common;

public class EventuateRestoreFromSnapshotFailedUnexpectedlyException extends EventuateClientException {
  public EventuateRestoreFromSnapshotFailedUnexpectedlyException(ReflectiveOperationException e) {
    super(e);
  }
}
