package com.networknt.eventuate.common;

import java.util.Optional;

public class SaveOptions {

  private Optional<String> entityId = Optional.empty();
  private Optional<EventContext> triggeringEvent = Optional.empty();


  public Optional<String> getEntityId() {
    return entityId;
  }

  public Optional<EventContext> getTriggeringEvent() {
    return triggeringEvent;
  }

  public SaveOptions withId(String entityId) {
    this.entityId = Optional.of(entityId);
    return this;
  }

  public SaveOptions withId(Optional<String> entityId) {
    this.entityId = entityId;
    return this;
  }

  public SaveOptions withEventContext(EventContext eventContext) {
    this.triggeringEvent = Optional.of(eventContext);
    return this;
  }
}
