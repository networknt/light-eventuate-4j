package com.networknt.eventuate.common;

import java.util.Optional;
import java.util.Map;

public class SaveOptions {

  private Optional<String> entityId = Optional.empty();
  private Optional<EventContext> triggeringEvent = Optional.empty();
  private Optional<Map<String, String>> eventMetadata = Optional.empty();

  public SaveOptions(Optional<String> entityId, Optional<EventContext> triggeringEvent) {
    this.entityId = entityId;
    this.triggeringEvent = triggeringEvent;
    this.eventMetadata = Optional.empty();
  }

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
