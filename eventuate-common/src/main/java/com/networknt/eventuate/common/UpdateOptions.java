package com.networknt.eventuate.common;

import com.networknt.eventuate.common.impl.SerializedSnapshot;

import java.util.Optional;
import java.util.Map;

public class UpdateOptions {

  private final Optional<EventContext> triggeringEvent;
  private final Optional<Map<String, String>> eventMetadata;
  private final Optional<Snapshot> snapshot;
  private final Optional<AggregateRepositoryInterceptor> interceptor;

  public UpdateOptions() {
    this.triggeringEvent = Optional.empty();
    this.eventMetadata = Optional.empty();
    this.snapshot = Optional.empty();
    this.interceptor= Optional.empty();
  }

  public UpdateOptions(Optional<EventContext> triggeringEvent, Optional<Map<String, String>> eventMetadata, Optional<Snapshot> snapshot, Optional<AggregateRepositoryInterceptor> interceptor) {
    this.triggeringEvent = triggeringEvent;
    this.eventMetadata = eventMetadata;
    this.snapshot = snapshot;
    this.interceptor= interceptor;
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

  public Optional<Map<String, String>> getEventMetadata() {
    return eventMetadata;
  }

  public Optional<Snapshot> getSnapshot() {
    return snapshot;
  }

  public UpdateOptions withTriggeringEvent(EventContext eventContext) {
    return new UpdateOptions(Optional.ofNullable(eventContext), this.eventMetadata, this.snapshot, this.interceptor);
  }

  public UpdateOptions withEventMetadata(Map<String, String> eventMetadata) {
    return new UpdateOptions(this.triggeringEvent, Optional.of(eventMetadata), this.snapshot, this.interceptor);
  }

  public UpdateOptions withSnapshot(Snapshot snapshot) {
    return new UpdateOptions(this.triggeringEvent, this.eventMetadata, Optional.of(snapshot), this.interceptor);
  }

  public UpdateOptions withInterceptor(AggregateRepositoryInterceptor interceptor) {
    return new UpdateOptions(this.triggeringEvent, this.eventMetadata, this.snapshot, Optional.of(interceptor));
  }

  public UpdateOptions withIdempotencyKey(String idempotencyKey) {
    // TODO - implement me
    return this;
  }

  public Optional<AggregateRepositoryInterceptor> getInterceptor() {
    return interceptor;
  }
}
