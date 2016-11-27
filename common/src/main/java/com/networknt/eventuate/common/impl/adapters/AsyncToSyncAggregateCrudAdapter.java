package com.networknt.eventuate.common.impl.adapters;

import com.networknt.eventuate.common.*;
import com.networknt.eventuate.common.impl.*;

import java.util.List;
import java.util.Optional;

public class AsyncToSyncAggregateCrudAdapter implements com.networknt.eventuate.common.impl.sync.AggregateCrud {

  private com.networknt.eventuate.common.impl.AggregateCrud target;

  private AsyncToSyncTimeoutOptions timeoutOptions = new AsyncToSyncTimeoutOptions();

  public AsyncToSyncAggregateCrudAdapter(AggregateCrud target) {
    this.target = target;
  }

  @Override
  public EntityIdVersionAndEventIds save(String aggregateType, List<EventTypeAndData> events, Optional<SaveOptions> options) {
    try {
      return target.save(aggregateType, events, options).get(timeoutOptions.getTimeout(), timeoutOptions.getTimeUnit());
    } catch (Throwable e) {
      Throwable unwrapped = CompletableFutureUtil.unwrap(e);
      if (unwrapped instanceof RuntimeException)
        throw (RuntimeException)unwrapped;
      else
        throw new RuntimeException(unwrapped);
    }
  }

  @Override
  public <T extends Aggregate<T>> LoadedEvents find(String aggregateType, String entityId, Optional<FindOptions> findOptions) {
    try {
      return target.find(aggregateType, entityId, findOptions).get(timeoutOptions.getTimeout(), timeoutOptions.getTimeUnit());
    } catch (Throwable e) {
      Throwable unwrapped = CompletableFutureUtil.unwrap(e);
      if (unwrapped instanceof RuntimeException)
        throw (RuntimeException)unwrapped;
      else
        throw new RuntimeException(unwrapped);
    }
  }

  @Override
  public EntityIdVersionAndEventIds update(EntityIdAndType entityIdAndType, Int128 entityVersion, List<EventTypeAndData> events, Optional<UpdateOptions> updateOptions) {
    try {
      return target.update(entityIdAndType, entityVersion, events, updateOptions).get(timeoutOptions.getTimeout(), timeoutOptions.getTimeUnit());
    } catch (Throwable e) {
      Throwable unwrapped = CompletableFutureUtil.unwrap(e);
      if (unwrapped instanceof RuntimeException)
        throw (RuntimeException)unwrapped;
      else
        throw new RuntimeException(unwrapped);
    }
  }

  public void setTimeoutOptions(AsyncToSyncTimeoutOptions timeoutOptions) {
    this.timeoutOptions = timeoutOptions;
  }
}
