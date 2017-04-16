package com.networknt.eventuate.common.impl;

import com.networknt.eventuate.common.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.networknt.eventuate.common.impl.EventuateActivity.activityLogger;

public class EventuateAggregateStoreImpl implements EventuateAggregateStore {

  private AggregateCrud aggregateCrud;
  private AggregateEvents aggregateEvents;
  private SnapshotManager snapshotManager;
  private SerializedEventDeserializer serializedEventDeserializer = new DefaultSerializedEventDeserializer();

  public EventuateAggregateStoreImpl(AggregateCrud aggregateCrud, AggregateEvents aggregateEvents,SnapshotManager snapshotManager) {
    this.aggregateCrud = aggregateCrud;
    this.aggregateEvents = aggregateEvents;
    this.snapshotManager = snapshotManager;
  }

  public void setSerializedEventDeserializer(SerializedEventDeserializer serializedEventDeserializer) {
    this.serializedEventDeserializer = serializedEventDeserializer;
  }

  @Override
  public <T extends Aggregate<T>> CompletableFuture<EntityIdAndVersion> save(Class<T> clasz, List<Event> events) {
    return save(clasz, events, Optional.empty());
  }

  @Override
  public <T extends Aggregate<T>> CompletableFuture<EntityIdAndVersion> save(Class<T> clasz, List<Event> events, SaveOptions saveOptions) {
    return save(clasz, events, Optional.ofNullable(saveOptions));
  }

  @Override
  public <T extends Aggregate<T>> CompletableFuture<EntityIdAndVersion> save(Class<T> clasz, List<Event> events, Optional<SaveOptions> saveOptions) {
    List<EventTypeAndData> serializedEvents = events.stream().map(this::toEventTypeAndData).collect(Collectors.toList());
    CompletableFuture<EntityIdVersionAndEventIds> outcome = aggregateCrud.save(clasz.getName(), serializedEvents, saveOptions);
    if (activityLogger.isDebugEnabled())
      return CompletableFutureUtil.tap(outcome, (result, throwable) -> {
        if (throwable == null)
          activityLogger.debug("Saved entity: {} {} {}", clasz.getName(), result.getEntityId(), toSerializedEventsWithIds(serializedEvents, result.getEventIds()));
        else
          activityLogger.error(String.format("Save entity failed: %s", clasz.getName()), throwable);
      }).thenApply(EntityIdVersionAndEventIds::toEntityIdAndVersion);
    else
      return outcome.thenApply(EntityIdVersionAndEventIds::toEntityIdAndVersion);
  }

  private List<EventIdTypeAndData> toSerializedEventsWithIds(List<EventTypeAndData> serializedEvents, List<Int128> eventIds) {
    return IntStream.range(0, serializedEvents.size()).boxed().map(idx ->
            new EventIdTypeAndData(eventIds.get(idx),
                    serializedEvents.get(idx).getEventType(),
                    serializedEvents.get(idx).getEventData())).collect(Collectors.toList());
  }


  @Override
  public <T extends Aggregate<T>> CompletableFuture<EntityWithMetadata<T>> find(Class<T> clasz, String entityId) {
    return find(clasz, entityId, Optional.empty());
  }

  @Override
  public <T extends Aggregate<T>> CompletableFuture<EntityWithMetadata<T>> find(Class<T> clasz, String entityId, FindOptions findOptions) {
    return find(clasz, entityId, Optional.ofNullable(findOptions));
  }

  private EventTypeAndData toEventTypeAndData(Event event) {
    return new EventTypeAndData(event.getClass().getName(), JSonMapper.toJson(event));
  }

  @Override
  public <T extends Aggregate<T>> CompletableFuture<EntityWithMetadata<T>> find(Class<T> clasz, String entityId, Optional<FindOptions> findOptions) {
    CompletableFuture<LoadedEvents> outcome = aggregateCrud.find(clasz.getName(), entityId, findOptions);

    CompletableFuture<LoadedEvents> tappedOutcome;
    if (activityLogger.isDebugEnabled())
      tappedOutcome = CompletableFutureUtil.tap(outcome, (result, throwable) -> {
        if (throwable == null)
          activityLogger.debug("Loaded entity: {} {} {}", clasz.getName(), entityId, result.getEvents());
        else
          activityLogger.error(String.format("Find entity failed: %s %s", clasz.getName(), entityId), throwable);
      });
    else
      tappedOutcome = outcome;

    return tappedOutcome.thenApply(le -> {
      List<Event> events = le.getEvents().stream().map(this::toEvent).collect(Collectors.toList());
      return new EntityWithMetadata<T>(
              new EntityIdAndVersion(entityId, le.getEvents().get(le.getEvents().size() - 1).getId()),
              events,
              Aggregates.recreateAggregate(clasz, events));
    });
  }

  @Override
  public <T extends Aggregate<T>> CompletableFuture<EntityIdAndVersion> update(Class<T> clasz, EntityIdAndVersion entityIdAndVersion, List<Event> events) {
    return update(clasz, entityIdAndVersion, events, Optional.empty());
  }

  @Override
  public <T extends Aggregate<T>> CompletableFuture<EntityIdAndVersion> update(Class<T> clasz, EntityIdAndVersion entityIdAndVersion, List<Event> events, UpdateOptions updateOptions) {
    return update(clasz, entityIdAndVersion, events, Optional.ofNullable(updateOptions));
  }

  private Event toEvent(EventIdTypeAndData eventIdTypeAndData) {
    try {
      return JSonMapper.fromJson(eventIdTypeAndData.getEventData(), (Class<Event>) Class.forName(eventIdTypeAndData.getEventType()));
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }


  @Override
  public <T extends Aggregate<T>> CompletableFuture<EntityIdAndVersion> update(Class<T> clasz, EntityIdAndVersion entityIdAndVersion, List<Event> events, Optional<UpdateOptions> updateOptions) {
    List<EventTypeAndData> serializedEvents = events.stream().map(this::toEventTypeAndData).collect(Collectors.toList());
    CompletableFuture<EntityIdVersionAndEventIds> outcome = aggregateCrud.update(new EntityIdAndType(entityIdAndVersion.getEntityId(), clasz.getName()),
            entityIdAndVersion.getEntityVersion(),
            serializedEvents,
            updateOptions);
    if (activityLogger.isDebugEnabled())
      return CompletableFutureUtil.tap(outcome, (result, throwable) -> {
        if (throwable == null)
          activityLogger.debug("Updated entity: {} {} {}", clasz.getName(), result.getEntityId(), toSerializedEventsWithIds(serializedEvents, result.getEventIds()));
        else
          activityLogger.error(String.format("Update entity failed: %s %s", clasz.getName(), entityIdAndVersion), throwable);
      }).thenApply(EntityIdVersionAndEventIds::toEntityIdAndVersion);
    else
      return outcome.thenApply(EntityIdVersionAndEventIds::toEntityIdAndVersion);
  }

  @Override
  public CompletableFuture<?> subscribe(String subscriberId, Map<String, Set<String>> aggregatesAndEvents, SubscriberOptions subscriberOptions, Function<DispatchedEvent<Event>, CompletableFuture<?>> handler) {
    if (activityLogger.isDebugEnabled())
      activityLogger.debug("Subscribing {} {}", subscriberId, aggregatesAndEvents);
    CompletableFuture<?> outcome = aggregateEvents.subscribe(subscriberId, aggregatesAndEvents, subscriberOptions,
            se -> serializedEventDeserializer.toDispatchedEvent(se).map(handler::apply).orElse(CompletableFuture.completedFuture(null)));
    if (activityLogger.isDebugEnabled())
      return CompletableFutureUtil.tap(outcome, (result, throwable) -> {
        if (throwable == null)
          activityLogger.debug("Subscribed {} {}", subscriberId, aggregatesAndEvents);
        else
          activityLogger.error(String.format("Subscribe failed: %s %s", subscriberId, aggregatesAndEvents), throwable);
      });
    else
      return outcome;
  }

  @Override
  public Optional<Snapshot> possiblySnapshot(Aggregate aggregate, Optional<Int128> snapshotVersion, List<Event> oldEvents, List<Event> newEvents) {
    return snapshotManager.possiblySnapshot(aggregate, snapshotVersion, oldEvents, newEvents);
  }

  @Override
  public Aggregate recreateFromSnapshot(Class<?> clasz, Snapshot snapshot) {
    return snapshotManager.recreateFromSnapshot(clasz, snapshot);
  }

}
