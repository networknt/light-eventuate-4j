package com.networknt.eventuate.kafka.producer;

import com.networknt.eventuate.server.common.PublishedEvent;
import org.apache.kafka.clients.producer.MockProducer;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Optional;


public class EventuateKafkaProducerTest {

    static EventuateKafkaProducer eventuateKafkaProducer;
    static PublishedEvent event;

    @BeforeClass
    public static void setUp() {
        eventuateKafkaProducer = new EventuateKafkaProducer();
        eventuateKafkaProducer.setProducer( new MockProducer(true, null , null, null) );
        event = new PublishedEvent("eventId", "entityId", "entityType", "eventJson", "eventType", null, Optional.of("metadata"));
    }


    @Test
    public void testSend() {
        eventuateKafkaProducer.send(event.getEntityType(), event.getId(), event.getEventData());
     }

    @Test(expected=IllegalArgumentException.class)
    public void testSendWithoutTopic() {
         eventuateKafkaProducer.send(null, event.getId(), event.getEventData());
    }
}
