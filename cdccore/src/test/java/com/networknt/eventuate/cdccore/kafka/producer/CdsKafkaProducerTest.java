package com.networknt.eventuate.cdccore.kafka.producer;

import com.networknt.eventuate.cdccore.PublishedEvent;
import com.networknt.eventuate.cdccore.kafka.consumer.TopicPartitionOffsets;
import org.apache.kafka.clients.producer.MockProducer;
import org.junit.BeforeClass;
import org.junit.Test;


public class CdsKafkaProducerTest {

    static CdcKafkaProducer cdcKafkaProducer;
    static PublishedEvent event;

    @BeforeClass
    public static void setUp() {
        cdcKafkaProducer = new CdcKafkaProducer();
        cdcKafkaProducer.setProducer( new MockProducer(true, null , null, null) );
         event = new PublishedEvent("eventId", "entityId", "entityType", "eventJson", "eventType");
    }


    @Test
    public void testSend() {
        cdcKafkaProducer.send(event.getEntityType(), event.getId(), event.getEventData());
     }

    @Test(expected=IllegalArgumentException.class)
    public void testSendWithoutTopic() {
         cdcKafkaProducer.send(null, event.getId(), event.getEventData());
    }
}
