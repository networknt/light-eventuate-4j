package com.networknt.eventuate.common.impl.sync;

import com.networknt.eventuate.common.*;
import com.networknt.eventuate.common.impl.EntityIdVersionAndEventIds;
import com.networknt.eventuate.common.impl.EventTypeAndData;
import com.networknt.eventuate.common.impl.LoadedEvents;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface AggregateCrud {
  EntityIdVersionAndEventIds save(String aggregateType, List<EventTypeAndData> events, Optional<SaveOptions> options);

  <T extends Aggregate<T>> LoadedEvents find(String aggregateType, String entityId, Optional<FindOptions> findOptions);

  EntityIdVersionAndEventIds update(EntityIdAndType entityIdAndType, Int128 entityVersion, List<EventTypeAndData> events, Optional<UpdateOptions> updateOptions);
}
