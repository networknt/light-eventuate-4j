package com.networknt.eventuate.test.util;

public class EventuallyException extends RuntimeException {
  public EventuallyException(String message, Throwable cause) {
    super(message, cause);
  }
}
