package com.networknt.eventuate.common.impl;

import com.networknt.eventuate.common.EntityIdAndVersion;
import com.networknt.eventuate.common.Int128;

import java.util.List;

public class EntityIdVersionAndEventIds {

  private final String entityId;
  private final Int128 entityVersion;
  private final List<Int128> eventIds;

  @Override
  public String toString() {
    return "EntityIdVersionAndEventIds{" +
            "entityId='" + entityId + '\'' +
            ", entityVersion=" + entityVersion +
            ", eventIds=" + eventIds +
            '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    EntityIdVersionAndEventIds that = (EntityIdVersionAndEventIds) o;

    if (entityId != null ? !entityId.equals(that.entityId) : that.entityId != null) return false;
    if (entityVersion != null ? !entityVersion.equals(that.entityVersion) : that.entityVersion != null) return false;
    return eventIds != null ? eventIds.equals(that.eventIds) : that.eventIds == null;

  }

  @Override
  public int hashCode() {
    int result = entityId != null ? entityId.hashCode() : 0;
    result = 31 * result + (entityVersion != null ? entityVersion.hashCode() : 0);
    result = 31 * result + (eventIds != null ? eventIds.hashCode() : 0);
    return result;
  }

  public EntityIdVersionAndEventIds(String entityId, Int128 entityVersion, List<Int128> eventIds) {
    this.entityId = entityId;
    this.entityVersion = entityVersion;
    this.eventIds = eventIds;
  }

  public String getEntityId() {
    return entityId;
  }

  public List<Int128> getEventIds() {
    return eventIds;
  }

  public Int128 getEntityVersion() {
    return entityVersion;
  }

  public EntityIdAndVersion toEntityIdAndVersion() {
    return new EntityIdAndVersion(entityId, entityVersion);
  }
}
