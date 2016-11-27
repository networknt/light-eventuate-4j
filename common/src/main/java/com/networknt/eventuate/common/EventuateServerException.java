package com.networknt.eventuate.common;

public class EventuateServerException extends EventuateException {

  public EventuateServerException() {
    super("An internal server error occurred");
  }
}
