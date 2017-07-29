package com.networknt.eventuate.common;

public class DefaultAggregateRepositoryInterceptor<T extends CommandProcessingAggregate<T, CT>, CT extends Command> implements AggregateRepositoryInterceptor<T, CT> {
}
