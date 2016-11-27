package com.networknt.eventuate.common;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A convenience class, with a reactive-style API that provides a simplified interface for creating and updating aggregates.
 * @param <T> the aggregate class, which is a subtype of CommandProcessingAggregate
 * @param <CT> the aggregate's command class, a subtype of command
 *
 * <p>For example:
 *
 * <pre class="code">
 * public class AccountService {
 *   private final AggregateRepository&gt;Account, AccountCommand&gt; accountRepository;
 *
 *   public AccountService(AggregateRepository&gt;Account, AccountCommand&gt; accountRepository) {
 *     this.accountRepository = accountRepository;
 *   }
 *
 *   public CompletableFuture&gt;EntityWithIdAndVersion&gt;Account&gt;&gt; openAccount(BigDecimal initialBalance) {
 *     return accountRepository.save(new CreateAccountCommand(initialBalance));
 *   }
 * }
 *</pre>
 *
 * @see CommandProcessingAggregate
 * @see Command
 *
 *
 */


public class AggregateRepository<T extends CommandProcessingAggregate<T, CT>, CT extends Command> {

  private static Logger logger = LoggerFactory.getLogger(AggregateRepository.class);

  private Class<T> clasz;
  private EventuateAggregateStore aggregateStore;

  /**
   * Constructs a new AggregateRepository for the specified aggregate class and aggregate store
   * @param clasz the class of the aggregate
   * @param aggregateStore the aggregate store
   */
  public AggregateRepository(Class<T> clasz, EventuateAggregateStore aggregateStore) {
    this.clasz = clasz;
    this.aggregateStore = aggregateStore;
  }

  /**
   * Create a new Aggregate by processing a command and persisting the events
   * @param cmd the command to process
   * @return the newly persisted aggregate
   */
  public CompletableFuture<EntityWithIdAndVersion<T>> save(CT cmd) {
    return save(cmd, Optional.empty());
  }

  /**
   *
   * Create a new Aggregate by processing a command and persisting the events
   * @param cmd the command to process
   * @param saveOptions creation options
   * @return the newly persisted aggregate
   */
  public CompletableFuture<EntityWithIdAndVersion<T>> save(CT cmd, Optional<SaveOptions> saveOptions) {
    T aggregate;
    try {
      aggregate = clasz.newInstance();
    } catch (InstantiationException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
    List<Event> events = aggregate.processCommand(cmd);
    Aggregates.applyEventsToMutableAggregate(aggregate, events);

    return aggregateStore.save(clasz, events, saveOptions).thenApply(entityIdAndVersion -> new EntityWithIdAndVersion<>(entityIdAndVersion, aggregate));
  }

  /**
   * Update the specified aggregate by processing a command and saving events
   * @param entityId the id of the aggregate to update
   * @param cmd the command to process
   * @return the updated and persisted aggregate
   */
  public CompletableFuture<EntityWithIdAndVersion<T>> update(String entityId, final CT cmd) {
    return update(entityId, cmd, Optional.empty());
  }

  class LoadedEntityWithMetadata {
    boolean success;
    EntityWithMetadata<T> ewmd;

    LoadedEntityWithMetadata(boolean success, EntityWithMetadata<T> ewmd) {
      this.success = success;
      this.ewmd = ewmd;
    }
  }

  private <T> CompletableFuture<T> withRetry(Supplier<CompletableFuture<T>> asyncRequest) {
    CompletableFuture<T> result = new CompletableFuture<>();
    attemptOperation(asyncRequest, result, 0);
    return result;
  }

  private <T> void attemptOperation(Supplier<CompletableFuture<T>> asyncRequest, CompletableFuture<T> result, int attempt) {
    CompletableFuture<T> f = asyncRequest.get();
    f.handleAsync((val, throwable) -> {
      if (throwable != null) {
        if (attempt < 10 && CompletableFutureUtil.unwrap(throwable) instanceof OptimisticLockingException) {
          logger.debug("got optimistic locking exception - retrying", throwable);
          attemptOperation(asyncRequest, result, attempt + 1);
        } else {
          if (logger.isDebugEnabled())
            logger.debug("got exception - NOT retrying: " + attempt, throwable);
          result.completeExceptionally(throwable);
        }
      }
      else
        result.complete(val);
      return null;
    });
  }


  /**
   * Update the specified aggregate by processing a command and saving events
   * @param entityId the id of the aggregate to update
   * @param cmd the command to process
   * @param updateOptions options for updating
   * @return the updated and persisted aggregate
   */
  public CompletableFuture<EntityWithIdAndVersion<T>> update(final String entityId, final CT cmd, Optional<UpdateOptions> updateOptions) {
      return updateWithProvidedCommand(entityId, (a) -> Optional.of(cmd), updateOptions);
  }

  /**
   * Update the specified aggregate by processing a command and saving events
   * @param entityId the id of the aggregate to update
   * @param commandProvider the provider of the command to process
   * @param updateOptions options for updating
   * @return the updated and persisted aggregate
   */
  public CompletableFuture<EntityWithIdAndVersion<T>> updateWithProvidedCommand(final String entityId, final Function<T, Optional<CT>> commandProvider, Optional<UpdateOptions> updateOptions) {

    return withRetry( () -> {
      CompletableFuture<LoadedEntityWithMetadata> eo = aggregateStore.find(clasz, entityId, updateOptions.map(uo -> new FindOptions().withTriggeringEvent(uo.getTriggeringEvent())))
              .handleAsync((tEntityWithMetadata, throwable) -> {
                if (throwable == null)
                  return new LoadedEntityWithMetadata(true, tEntityWithMetadata);
                else {
                  logger.debug("Exception finding aggregate", throwable);
                  Throwable unwrapped = CompletableFutureUtil.unwrap(throwable);
                  if (unwrapped instanceof DuplicateTriggeringEventException)
                    return new LoadedEntityWithMetadata(false, null);
                  else if (unwrapped instanceof RuntimeException)
                    throw (RuntimeException) unwrapped;
                  else if (throwable instanceof RuntimeException)
                    throw (RuntimeException) throwable;
                  else
                    // TODO - does this make sense?
                    throw new RuntimeException(throwable);
                }
              });

      return eo.thenCompose(loadedEntityWithMetadata -> {
        if (loadedEntityWithMetadata.success) {
          EntityWithMetadata<T> entityWithMetaData = loadedEntityWithMetadata.ewmd;
          final T aggregate = entityWithMetaData.getEntity();
          List<Event> events = commandProvider.apply(aggregate).map(aggregate::processCommand).orElse(Collections.emptyList());
          if (events.isEmpty()) {
            return CompletableFuture.completedFuture(entityWithMetaData.toEntityWithIdAndVersion());
          } else {
            return aggregateStore.update(clasz, entityWithMetaData.getEntityIdAndVersion(), events, updateOptions)
                    .thenApply(entityIdAndVersion -> {
                      Aggregates.applyEventsToMutableAggregate(aggregate, events);
                      return new EntityWithIdAndVersion<T>(entityIdAndVersion, aggregate);
                    })
                    .handle((r, t) -> {
                      if (t == null)
                        return r;
                      else {
                        logger.debug("Exception updating aggregate", t);
                        Throwable unwrapped = CompletableFutureUtil.unwrap(t);
                        if (unwrapped instanceof DuplicateTriggeringEventException)
                          return new EntityWithIdAndVersion<T>(entityWithMetaData.getEntityIdAndVersion(), aggregate);
                        else if (unwrapped instanceof RuntimeException)
                          throw (RuntimeException) unwrapped;
                        else if (t instanceof RuntimeException)
                          throw (RuntimeException) t;
                        else
                          throw new RuntimeException(unwrapped);
                      }
                    });
          }
        } else {
          return aggregateStore.find(clasz, entityId, Optional.empty()).thenApply(EntityWithMetadata::toEntityWithIdAndVersion);
        }
      });
    });
  }

  /**
   * Find an aggregate
   * @param entityId the id of the aggregate to find
   * @return the aggregate
   */
  public CompletableFuture<EntityWithMetadata<T>> find(String entityId) {
    return aggregateStore.find(clasz, entityId);
  }

  /**
   * Find an aggregate
   * @param entityId the id of the aggregate to find
   * @param findOptions options for finding
   * @return the aggregate
   */
  public CompletableFuture<EntityWithMetadata<T>> find(String entityId, FindOptions findOptions) {
    return aggregateStore.find(clasz, entityId, findOptions);
  }

  /**
   * Find an aggregate
   * @param entityId the id of the aggregate to find
   * @param findOptions options for finding
   * @return the aggregate
   */
  public CompletableFuture<EntityWithMetadata<T>> find(String entityId, Optional<FindOptions> findOptions) {
    return aggregateStore.find(clasz, entityId, findOptions);
  }
}

