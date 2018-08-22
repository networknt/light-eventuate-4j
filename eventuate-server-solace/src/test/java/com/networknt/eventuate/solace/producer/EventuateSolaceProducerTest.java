package com.networknt.eventuate.solace.producer;

import com.networknt.eventuate.server.common.PublishedEvent;
import com.solacesystems.jcsmp.JCSMPException;
import org.apache.kafka.clients.producer.MockProducer;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Optional;


public class EventuateSolaceProducerTest {

    static EventuateSolaceProducer eventuateSolaceProducer;
    static PublishedEvent event;

    @BeforeClass
    public static void setUp() throws JCSMPException {
        eventuateSolaceProducer = new EventuateSolaceProducer();
        event = new PublishedEvent("eventId", "entityId", "entityType", "eventJson", "eventType", null, Optional.of("metadata111111111222"));
    }


    @Test
    public void testSend()  {
   /*     try {
            eventuateSolaceProducer.send(event.getEntityType(), event.getEventData());
        } catch (JCSMPException e) {
            System.out.println("error on send message:" + e);
        }*/
     }


}
