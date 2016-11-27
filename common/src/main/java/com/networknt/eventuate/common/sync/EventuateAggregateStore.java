package com.networknt.eventuate.common.sync;

import com.networknt.eventuate.common.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * The synchronous-style interface to the event store
 */
public interface EventuateAggregateStore {

  <T extends Aggregate<T>> EntityIdAndVersion save(Class<T> clasz, List<Event> events);
  <T extends Aggregate<T>> EntityIdAndVersion save(Class<T> clasz, List<Event> events, SaveOptions saveOptions);
  <T extends Aggregate<T>> EntityIdAndVersion save(Class<T> clasz, List<Event> events, Optional<SaveOptions> saveOptions);

  <T extends Aggregate<T>> EntityWithMetadata<T> find(Class<T> clasz, String entityId);
  <T extends Aggregate<T>> EntityWithMetadata<T> find(Class<T> clasz, String entityId, FindOptions findOptions);
  <T extends Aggregate<T>> EntityWithMetadata<T> find(Class<T> clasz, String entityId, Optional<FindOptions> findOptions);

  <T extends Aggregate<T>> EntityIdAndVersion update(Class<T> clasz, EntityIdAndVersion entityIdAndVersion, List<Event> events);
  <T extends Aggregate<T>> EntityIdAndVersion update(Class<T> clasz, EntityIdAndVersion entityIdAndVersion, List<Event> events, UpdateOptions updateOptions);
  <T extends Aggregate<T>> EntityIdAndVersion update(Class<T> clasz, EntityIdAndVersion entityIdAndVersion, List<Event> events, Optional<UpdateOptions> updateOptions);

  void subscribe(String subscriberId, Map<String, Set<String>> aggregatesAndEvents, SubscriberOptions subscriberOptions, Function<DispatchedEvent<Event>, CompletableFuture<?>> dispatch);

}
