package com.networknt.eventuate.common.impl;

import com.networknt.eventuate.common.EventContext;

import java.util.Optional;

public class AggregateCrudUpdateOptions {

  private final Optional<EventContext> triggeringEvent;
  private final Optional<SerializedSnapshot> snapshot;

  public AggregateCrudUpdateOptions() {
    this.triggeringEvent = Optional.empty();
    this.snapshot = Optional.empty();
  }

  public AggregateCrudUpdateOptions(Optional<EventContext> triggeringEvent, Optional<SerializedSnapshot> snapshot) {
    this.triggeringEvent = triggeringEvent;
    this.snapshot = snapshot;
  }

  public AggregateCrudUpdateOptions withSnapshot(SerializedSnapshot serializedSnapshot) {
    return new AggregateCrudUpdateOptions(this.triggeringEvent, Optional.of(serializedSnapshot));
  }

  @Override
  public String toString() {
    return "AggregateCrudUpdateOptions{" +
        "triggeringEvent=" + triggeringEvent +
        ", snapshot='" + snapshot + '\'' +
      '}';
  }

  public Optional<EventContext> getTriggeringEvent() {
    return triggeringEvent;
  }

  public Optional<SerializedSnapshot> getSnapshot() {
    return snapshot;
  }

  public AggregateCrudUpdateOptions withTriggeringEvent(EventContext eventContext) {
    return new AggregateCrudUpdateOptions(Optional.of(eventContext), this.snapshot);
  }

}
