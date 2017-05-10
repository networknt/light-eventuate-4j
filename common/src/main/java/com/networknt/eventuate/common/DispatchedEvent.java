package com.networknt.eventuate.common;



/**
 * value object class for Dispatched Event
 *
 */
public class DispatchedEvent<T extends Event> implements EventEnvelope<T> {

  private String entityId;
  private Int128 eventId;
  private T event;
  private Integer swimlane;
  private final Long offset;
  private final EventContext eventContext;

  public DispatchedEvent(String entityId, Int128 eventId, T event, Integer swimlane, Long offset, EventContext eventContext) {
    this.entityId = entityId;
    this.eventId = eventId;
    this.event = event;
    this.swimlane = swimlane;
    this.offset = offset;
    this.eventContext = eventContext;
  }

  @Override
  public String toString() {
    return "DispatchedEvent{" +
            "entityId='" + entityId + '\'' +
            ", eventId=" + eventId +
            ", event=" + event +
            ", swimlane=" + swimlane +
            ", offset=" + offset +
            ", eventContext=" + eventContext +
            '}';
  }

  @Override
  public Int128 getEventId() {
    return eventId;
  }

  @Override
  public Class<T> getEventType() {
    return (Class<T>) event.getClass();
  }

  @Override
  public T getEvent() {
    return event;
  }

  @Override
  public String getEntityId() {
    return entityId;
  }

  @Override
  public Integer getSwimlane() {
    return swimlane;
  }

  @Override
  public Long getOffset() {
    return offset;
  }

  @Override
  public EventContext getEventContext() {
    return eventContext;
  }
}
