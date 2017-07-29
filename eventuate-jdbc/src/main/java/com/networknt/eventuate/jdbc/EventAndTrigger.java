package com.networknt.eventuate.jdbc;

import com.networknt.eventuate.common.impl.EventIdTypeAndData;

public class EventAndTrigger {

  public final EventIdTypeAndData event;
  public final String triggeringEvent;

  public EventAndTrigger(EventIdTypeAndData event, String triggeringEvent) {

    this.event = event;
    this.triggeringEvent = triggeringEvent;
  }
}
