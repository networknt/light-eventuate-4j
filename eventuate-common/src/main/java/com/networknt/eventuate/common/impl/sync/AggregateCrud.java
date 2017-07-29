package com.networknt.eventuate.common.impl.sync;

import com.networknt.eventuate.common.*;
import com.networknt.eventuate.common.impl.*;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Base interface for command side Aggregate Crud
 */
public interface AggregateCrud {

  /**
   * Create a new Aggregate by processing a command and persisting the events
   * @param aggregateType the aggregateType for event store
   * @param events the list of events for the aggregate
   * @param options the command options
   * @return the newly persisted EntityIdVersionAndEventIds
   */
  EntityIdVersionAndEventIds save(String aggregateType, List<EventTypeAndData> events, Optional<AggregateCrudSaveOptions> options);

  /**
   * Find a  Aggregate events by giving aggregateType and entityId
   * @param aggregateType the aggregateType for event store
   * @param findOptions the command options
   * @return the LoadedEvents
   */
  <T extends Aggregate<T>> LoadedEvents find(String aggregateType, String entityId, Optional<AggregateCrudFindOptions> findOptions);


  /**
   * Update the Aggregate by processing a command and persisting the events
   * @param entityIdAndType the aggregate id and  aggregateType for udpating on event store
   * @param entityVersion entity version number
   * @param events the list of events for the aggregate
   * @param updateOptions the command options
   * @return the updated EntityIdVersionAndEventIds
   */
  EntityIdVersionAndEventIds update(EntityIdAndType entityIdAndType, Int128 entityVersion, List<EventTypeAndData> events, Optional<AggregateCrudUpdateOptions> updateOptions);
}
