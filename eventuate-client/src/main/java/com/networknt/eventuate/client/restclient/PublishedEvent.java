package com.networknt.eventuate.client.restclient;

import com.networknt.eventuate.common.EntityIdAndType;
import com.networknt.eventuate.common.EventIdAndType;

public class PublishedEvent {

  private EventIdAndType eventIdAndType;
  private EntityIdAndType sender;

  public PublishedEvent(EventIdAndType eventIdAndType, EntityIdAndType sender) {
    this.eventIdAndType = eventIdAndType;
    this.sender = sender;
  }

  public EventIdAndType getEventIdAndType() {
    return eventIdAndType;
  }

  public EntityIdAndType getSender() {
    return sender;
  }
}
