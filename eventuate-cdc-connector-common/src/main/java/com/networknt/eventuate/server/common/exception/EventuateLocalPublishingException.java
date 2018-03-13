package com.networknt.eventuate.server.common.exception;

public class EventuateLocalPublishingException extends RuntimeException {
  public EventuateLocalPublishingException(String message, Exception cause) {
    super(message, cause);
  }
}
