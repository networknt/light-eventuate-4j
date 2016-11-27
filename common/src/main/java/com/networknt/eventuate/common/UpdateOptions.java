package com.networknt.eventuate.common;

import java.util.Optional;

public class UpdateOptions {

  private Optional<EventContext> triggeringEvent = Optional.empty();

  @Override
  public String toString() {
    return "UpdateOptions{" +
            "triggeringEvent=" + triggeringEvent +
            '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    UpdateOptions that = (UpdateOptions) o;

    return triggeringEvent != null ? triggeringEvent.equals(that.triggeringEvent) : that.triggeringEvent == null;

  }

  @Override
  public int hashCode() {
    return triggeringEvent != null ? triggeringEvent.hashCode() : 0;
  }

  public Optional<EventContext> getTriggeringEvent() {
    return triggeringEvent;
  }


  public UpdateOptions withTriggeringEvent(EventContext eventContext) {
    this.triggeringEvent = Optional.ofNullable(eventContext);
    return this;
  }


}
