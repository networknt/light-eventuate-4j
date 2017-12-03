package com.networknt.eventuate.jdbc;

import com.networknt.eventuate.common.Aggregate;
import com.networknt.eventuate.common.EntityIdAndType;
import com.networknt.eventuate.common.Int128;
import com.networknt.eventuate.common.impl.*;

import java.util.List;
import java.util.Optional;

public interface EventuateJdbcAccess {

  SaveUpdateResult save(String aggregateType, List<EventTypeAndData> events, Optional<AggregateCrudSaveOptions> saveOptions);

  <T extends Aggregate<T>> LoadedEvents find(String aggregateType, String entityId, Optional<AggregateCrudFindOptions> findOptions);

  SaveUpdateResult update(EntityIdAndType entityIdAndType, Int128 entityVersion, List<EventTypeAndData> events, Optional<AggregateCrudUpdateOptions> updateOptions);
}
