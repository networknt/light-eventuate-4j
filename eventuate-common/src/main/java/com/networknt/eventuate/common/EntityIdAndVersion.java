package com.networknt.eventuate.common;

/**
 * value object class for EntityId And Version
 *
 */
public class EntityIdAndVersion {

  private final String entityId;
  private final Int128 entityVersion;

  @Override
  public String toString() {
    return "EntityIdAndVersion{" +
            "entityId='" + entityId + '\'' +
            ", entityVersion=" + entityVersion +
            '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    EntityIdAndVersion that = (EntityIdAndVersion) o;

    if (entityId != null ? !entityId.equals(that.entityId) : that.entityId != null) return false;
    return entityVersion != null ? entityVersion.equals(that.entityVersion) : that.entityVersion == null;

  }

  @Override
  public int hashCode() {
    int result = entityId != null ? entityId.hashCode() : 0;
    result = 31 * result + (entityVersion != null ? entityVersion.hashCode() : 0);
    return result;
  }

  public EntityIdAndVersion(String entityId, Int128 entityVersion) {
    this.entityId = entityId;
    this.entityVersion = entityVersion;
  }

  public String getEntityId() {
    return entityId;
  }

  public Int128 getEntityVersion() {
    return entityVersion;
  }
}
