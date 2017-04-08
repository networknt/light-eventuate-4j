package com.networknt.eventuate.common;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EventEnvelopeUtil {

    public static List<EventEnvelope> eventEnvelope(EventEnvelope... eventEnvelopes) {
        return Arrays.asList(eventEnvelopes);
    }

    public static Map<String, Event> eventEnvelopeMapById(List<EventEnvelope> eventEnvelopes) {
        return eventEnvelopes.stream().collect(Collectors.toMap(EventEnvelope::getEntityId, EventEnvelope::getEvent));
    }

    public static Map<String, List<EventEnvelope>> eventEnvelopeGroupByType(List<EventEnvelope> eventEnvelopes) {
        //TODO
        return null;
    }

}