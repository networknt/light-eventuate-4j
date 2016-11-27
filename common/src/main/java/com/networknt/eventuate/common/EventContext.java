package com.networknt.eventuate.common;

public class EventContext {

  private String eventToken;

  public EventContext(String eventToken) {
    this.eventToken = eventToken;
  }

  @Override
  public String toString() {
    return "EventContext{" +
            "eventToken='" + eventToken + '\'' +
            '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    EventContext that = (EventContext) o;

    return eventToken != null ? eventToken.equals(that.eventToken) : that.eventToken == null;

  }

  @Override
  public int hashCode() {
    return eventToken != null ? eventToken.hashCode() : 0;
  }

  public String getEventToken() {
    return eventToken;
  }

}
