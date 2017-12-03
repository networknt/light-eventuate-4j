package com.networknt.eventuate.server.common;

public class AggregateTopicMapping {

  public static String aggregateTypeToTopic(String aggregateType) {
    return TopicCleaner.clean(aggregateType);
  }

}
