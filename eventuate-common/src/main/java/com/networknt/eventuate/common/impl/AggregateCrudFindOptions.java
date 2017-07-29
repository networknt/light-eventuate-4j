package com.networknt.eventuate.common.impl;

import com.networknt.eventuate.common.EventContext;

import java.util.Optional;

public class AggregateCrudFindOptions {

  private Optional<EventContext> triggeringEvent = Optional.empty();

  public AggregateCrudFindOptions() {
  }

  public AggregateCrudFindOptions(Optional<EventContext> triggeringEvent) {
    this.triggeringEvent = triggeringEvent;
  }

  public Optional<EventContext> getTriggeringEvent() {
    return triggeringEvent;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    AggregateCrudFindOptions that = (AggregateCrudFindOptions) o;

    return triggeringEvent != null ? triggeringEvent.equals(that.triggeringEvent) : that.triggeringEvent == null;
  }

  @Override
  public int hashCode() {
    return triggeringEvent != null ? triggeringEvent.hashCode() : 0;
  }

  public AggregateCrudFindOptions withTriggeringEvent(EventContext eventContext) {
    this.triggeringEvent = Optional.ofNullable(eventContext);
    return this;
  }

  public AggregateCrudFindOptions withTriggeringEvent(Optional<EventContext> eventContext) {
    this.triggeringEvent = eventContext;
    return this;
  }
}
