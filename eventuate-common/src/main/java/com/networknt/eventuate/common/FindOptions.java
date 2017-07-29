package com.networknt.eventuate.common;

import java.util.Optional;

public class FindOptions {

  private Optional<EventContext> triggeringEvent = Optional.empty();

  public Optional<EventContext> getTriggeringEvent() {
    return triggeringEvent;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    FindOptions that = (FindOptions) o;

    return triggeringEvent != null ? triggeringEvent.equals(that.triggeringEvent) : that.triggeringEvent == null;

  }

  @Override
  public int hashCode() {
    return triggeringEvent != null ? triggeringEvent.hashCode() : 0;
  }

  public FindOptions withTriggeringEvent(EventContext eventContext) {
    this.triggeringEvent = Optional.ofNullable(eventContext);
    return this;
  }

  public FindOptions withTriggeringEvent(Optional<EventContext> eventContext) {
    this.triggeringEvent = eventContext;
    return this;
  }
}
