package com.networknt.eventuate.common.impl.adapters;

import com.networknt.eventuate.common.*;
import com.networknt.eventuate.common.impl.*;
import com.networknt.eventuate.common.impl.EventTypeAndData;
import com.networknt.eventuate.common.impl.sync.AggregateCrud;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class SyncToAsyncAggregateCrudAdapter implements com.networknt.eventuate.common.impl.AggregateCrud {

  private com.networknt.eventuate.common.impl.sync.AggregateCrud target;

  public SyncToAsyncAggregateCrudAdapter(AggregateCrud target) {
    this.target = target;
  }

  @Override
  public CompletableFuture<EntityIdVersionAndEventIds> save(String aggregateType, List<EventTypeAndData> events, Optional<AggregateCrudSaveOptions> options) {
    try {
      return CompletableFuture.completedFuture(target.save(aggregateType, events, options));
    } catch (Exception e) {
      return CompletableFutureUtil.failedFuture(e);
    }
  }

  @Override
  public <T extends Aggregate<T>> CompletableFuture<LoadedEvents> find(String aggregateType, String entityId, Optional<AggregateCrudFindOptions> findOptions) {
    try {
      return CompletableFuture.completedFuture(target.find(aggregateType, entityId, findOptions));
    } catch (Exception e) {
      return CompletableFutureUtil.failedFuture(e);
    }
  }

  @Override
  public CompletableFuture<EntityIdVersionAndEventIds> update(EntityIdAndType entityIdAndType, Int128 entityVersion, List<EventTypeAndData> events, Optional<AggregateCrudUpdateOptions> updateOptions) {
    try {
      return CompletableFuture.completedFuture(target.update(entityIdAndType, entityVersion, events, updateOptions));
    } catch (Exception e) {
      return CompletableFutureUtil.failedFuture(e);
    }
  }
}
