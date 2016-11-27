package com.networknt.eventuate.common;


public class EventIdAndType {

  private Int128 id;
  private String eventType;

  public EventIdAndType() {
  }

  public EventIdAndType(Int128 id, String eventType) {
    this.id = id;
    this.eventType = eventType;
  }

  public Int128 getId() {
    return id;
  }

  public void setId(Int128 id) {
    this.id = id;
  }

  public String getEventType() {
    return eventType;
  }

  public void setEventType(String eventType) {
    this.eventType = eventType;
  }
}
