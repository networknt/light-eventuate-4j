package com.networknt.eventuate.jdbc;

import com.networknt.eventuate.common.impl.EntityIdVersionAndEventIds;

public class SaveUpdateResult  {
  private final EntityIdVersionAndEventIds entityIdVersionAndEventIds;
  private final PublishableEvents publishableEvents;

  public SaveUpdateResult(EntityIdVersionAndEventIds entityIdVersionAndEventIds, PublishableEvents publishableEvents) {
    this.entityIdVersionAndEventIds = entityIdVersionAndEventIds;
    this.publishableEvents = publishableEvents;
  }

  public EntityIdVersionAndEventIds getEntityIdVersionAndEventIds() {
    return entityIdVersionAndEventIds;
  }

  public PublishableEvents getPublishableEvents() {
    return publishableEvents;
  }
}
