package com.networknt.eventuate.common;

public class EventuateCommandProcessingFailedUnexpectedlyException extends EventuateClientException {
  public EventuateCommandProcessingFailedUnexpectedlyException(Throwable t) {
    super(t);
  }
}
