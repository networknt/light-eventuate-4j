package com.networknt.eventuate.client.restclient;

import com.networknt.eventuate.common.impl.EventIdTypeAndData;

import java.util.List;

public class GetEntityResponse {

  private List<EventIdTypeAndData> events;

    public GetEntityResponse() {
  }


  public GetEntityResponse(List<EventIdTypeAndData> events) {
    this.events = events;
  }

  public List<EventIdTypeAndData> getEvents() {
    return events;
  }



  public void setEvents(List<EventIdTypeAndData> events) {
    this.events = events;
  }
}
