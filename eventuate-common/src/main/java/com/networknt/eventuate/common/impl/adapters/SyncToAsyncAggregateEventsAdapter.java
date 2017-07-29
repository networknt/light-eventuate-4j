package com.networknt.eventuate.common.impl.adapters;

import com.networknt.eventuate.common.CompletableFutureUtil;
import com.networknt.eventuate.common.SubscriberOptions;
import com.networknt.eventuate.common.impl.SerializedEvent;
import com.networknt.eventuate.common.impl.sync.AggregateEvents;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class SyncToAsyncAggregateEventsAdapter implements com.networknt.eventuate.common.impl.AggregateEvents {

  private com.networknt.eventuate.common.impl.sync.AggregateEvents target;

  public SyncToAsyncAggregateEventsAdapter(AggregateEvents target) {
    this.target = target;
  }

  @Override
  public CompletableFuture<?> subscribe(String subscriberId, Map<String, Set<String>> aggregatesAndEvents, SubscriberOptions subscriberOptions, Function<SerializedEvent, CompletableFuture<?>> handler) {
    try {
      target.subscribe(subscriberId, aggregatesAndEvents, subscriberOptions, handler);
      return CompletableFuture.completedFuture(null);
    } catch (RuntimeException e) {
      return CompletableFutureUtil.failedFuture(e);
    }
  }
}
