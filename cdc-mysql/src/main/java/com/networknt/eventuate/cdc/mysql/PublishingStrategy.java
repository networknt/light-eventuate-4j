package com.networknt.eventuate.cdc.mysql;

import java.util.Optional;

public interface PublishingStrategy<M> {


  String partitionKeyFor(M publishedEvent);

  String topicFor(M publishedEvent);

  String toJson(M eventInfo);

  Optional<Long> getCreateTime(M publishedEvent);
}
