package com.networknt.eventuate.common;

import com.networknt.eventuate.common.impl.SerializedSnapshot;

import java.util.Optional;
import java.util.Map;

public class UpdateOptions {

  private Optional<EventContext> triggeringEvent = Optional.empty();
  private final Optional<Map<String, String>> eventMetadata;
  private final Optional<Snapshot> snapshot;

  public UpdateOptions() {
    this.triggeringEvent = Optional.empty();
    this.eventMetadata = Optional.empty();
    this.snapshot = Optional.empty();
  }
  @Override
  public String toString() {
    return "UpdateOptions{" +
            "triggeringEvent=" + triggeringEvent +
            '}';
  }

  public UpdateOptions(Optional<EventContext> triggeringEvent, Optional<Map<String, String>> eventMetadata, Optional<Snapshot> snapshot) {
    this.triggeringEvent = triggeringEvent;
    this.eventMetadata = eventMetadata;
    this.snapshot = snapshot;
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

  public Optional<Map<String, String>>getEventMetadata() {
    return eventMetadata;
  }

  public Optional<Snapshot> getSnapshot() {
    return snapshot;
  }
  public UpdateOptions withSnapshot(Snapshot snapshot) {
    return new UpdateOptions(this.triggeringEvent, this.eventMetadata, Optional.of(snapshot));
  }
}
