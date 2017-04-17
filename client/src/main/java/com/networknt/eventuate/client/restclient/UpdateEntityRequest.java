package com.networknt.eventuate.client.restclient;

import com.networknt.eventuate.common.impl.EventTypeAndData;
import com.networknt.eventuate.common.Int128;

import java.util.List;

public class UpdateEntityRequest {

  private List<EventTypeAndData> events;
  private Int128 entityVersion;
  private String triggeringEventToken;

  public UpdateEntityRequest(List<EventTypeAndData> events, Int128 entityVersion, String eventToken) {
    this.events = events;
    this.entityVersion = entityVersion;
    this.triggeringEventToken = eventToken;
  }

  public List<EventTypeAndData> getEvents() {
    return events;
  }

  public Int128 getEntityVersion() {
    return entityVersion;
  }

  public String getTriggeringEventToken() {
    return triggeringEventToken;
  }
}

