package com.networknt.eventuate.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EventEnvelopeUtil {

    // private constructor to prevent create another instance.
    private EventEnvelopeUtil() {}

    private static final EventEnvelopeUtil instance = new EventEnvelopeUtil();

    // This is the only way to get instance
    public static EventEnvelopeUtil getInstance() {
        return instance;
    }

    public  List<EventEnvelope> eventEnvelope(EventEnvelope... eventEnvelopes) {
        return Arrays.asList(eventEnvelopes);
    }

    public  Map<String, Event> eventEnvelopeMapById(List<EventEnvelope> eventEnvelopes) {
        return eventEnvelopes.stream().collect(Collectors.toMap(EventEnvelope::getEntityId, EventEnvelope::getEvent));
    }

    public  Map<String, List<Event>> eventEnvelopeGroupByType(List<EventEnvelope> eventEnvelopes) {
       // return  eventEnvelopes.stream().collect(Collectors.groupingBy(EventEnvelope::getEventType, Collectors.mapping(EventEnvelope::getEvent, Collectors.toList())));
        return null;
    }

}