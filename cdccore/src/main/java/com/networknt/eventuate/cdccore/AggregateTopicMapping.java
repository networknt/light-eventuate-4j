package com.networknt.eventuate.cdccore;

/**
 * Created by stevehu on 2016-11-13.
 */
public class AggregateTopicMapping {
    public static String aggregateTypeToTopic(String aggregateType) {
        return aggregateType.replace("$", "_DLR_");
    }
}
