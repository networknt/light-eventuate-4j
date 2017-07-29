package com.networknt.eventuate.cdc.mysql.exception;

public class EventuateLocalPublishingException extends Exception {

  public EventuateLocalPublishingException(String message, Exception cause) {
    super(message, cause);
  }

}
