package com.networknt.eventuate.cdccore;

/**
 * AggregateTopicMapping fine the topic name mapping from aggregateType
 */
public class AggregateTopicMapping {
    public static String aggregateTypeToTopic(String aggregateType) {
        return aggregateType.replace("$", "_DLR_");
    }
}
