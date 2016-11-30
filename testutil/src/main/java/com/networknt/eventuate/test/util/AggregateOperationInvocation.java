package com.networknt.eventuate.test.util;

import com.networknt.eventuate.common.CommandProcessingAggregate;
import com.networknt.eventuate.common.EntityWithIdAndVersion;

public interface AggregateOperationInvocation<T extends CommandProcessingAggregate<T, CT>, CT, C extends CT> {
  EntityWithIdAndVersion<T> getEntity();

  C getCommand();
}
