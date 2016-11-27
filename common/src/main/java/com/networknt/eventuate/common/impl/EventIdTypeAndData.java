package com.networknt.eventuate.common.impl;

import com.networknt.eventuate.common.Int128;

public class EventIdTypeAndData {

  private Int128 id;
  private String eventType;
  private String eventData;

  public EventIdTypeAndData() {
  }

  public EventIdTypeAndData(Int128 id, String eventType, String eventData) {
    this.id = id;
    this.eventType = eventType;
    this.eventData = eventData;
  }

  @Override
  public String toString() {
    return "EventIdTypeAndData{" +
            "id=" + id +
            ", eventType='" + eventType + '\'' +
            ", eventData='" + eventData + '\'' +
            '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    EventIdTypeAndData that = (EventIdTypeAndData) o;

    if (id != null ? !id.equals(that.id) : that.id != null) return false;
    if (eventType != null ? !eventType.equals(that.eventType) : that.eventType != null) return false;
    return eventData != null ? eventData.equals(that.eventData) : that.eventData == null;

  }

  @Override
  public int hashCode() {
    int result = id != null ? id.hashCode() : 0;
    result = 31 * result + (eventType != null ? eventType.hashCode() : 0);
    result = 31 * result + (eventData != null ? eventData.hashCode() : 0);
    return result;
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

  public String getEventData() {
    return eventData;
  }

  public void setEventData(String eventData) {
    this.eventData = eventData;
  }
}
