package com.networknt.eventuate.common;

import java.util.List;

public class EntityWithMetadata<T extends Aggregate> {

  private EntityIdAndVersion entityIdAndVersion;
  private List<Event> events;

  public EntityWithMetadata(EntityIdAndVersion entityIdAndVersion, List<Event> events, T entity) {
    this.entityIdAndVersion = entityIdAndVersion;
    this.events = events;
    this.entity = entity;
  }

  private T entity;

  public T getEntity() {
    return entity;
  }

  public EntityIdAndVersion getEntityIdAndVersion() {
    return entityIdAndVersion;
  }

  public EntityWithIdAndVersion<T> toEntityWithIdAndVersion() {
    return new EntityWithIdAndVersion<T>(entityIdAndVersion, entity);
  }

  public List<Event> getEvents() {
    return events;
  }
}
