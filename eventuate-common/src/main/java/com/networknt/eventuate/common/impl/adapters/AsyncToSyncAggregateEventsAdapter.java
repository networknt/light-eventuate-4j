package com.networknt.eventuate.common.impl.adapters;

import com.networknt.eventuate.common.CompletableFutureUtil;
import com.networknt.eventuate.common.SubscriberOptions;
import com.networknt.eventuate.common.impl.AggregateEvents;
import com.networknt.eventuate.common.impl.SerializedEvent;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class AsyncToSyncAggregateEventsAdapter implements com.networknt.eventuate.common.impl.sync.AggregateEvents {

  private com.networknt.eventuate.common.impl.AggregateEvents target;
  private AsyncToSyncTimeoutOptions timeoutOptions = new AsyncToSyncTimeoutOptions();

  public AsyncToSyncAggregateEventsAdapter(AggregateEvents target) {
    this.target = target;
  }

  @Override
  public void subscribe(String subscriberId, Map<String, Set<String>> aggregatesAndEvents, SubscriberOptions subscriberOptions, Function<SerializedEvent, CompletableFuture<?>> handler) {
    try {
      target.subscribe(subscriberId, aggregatesAndEvents, subscriberOptions, handler).get(timeoutOptions.getTimeout(), timeoutOptions.getTimeUnit());
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
