package com.networknt.eventuate.server.jdbckafkastore;


import com.networknt.eventuate.common.Int128;
import com.networknt.eventuate.common.SubscriberOptions;
import com.networknt.eventuate.common.impl.AggregateEvents;
import com.networknt.eventuate.common.impl.JSonMapper;
import com.networknt.eventuate.common.impl.SerializedEvent;
import com.networknt.eventuate.kafka.consumer.EventuateKafkaConsumer;
import com.networknt.eventuate.server.common.AggregateTopicMapping;
import com.networknt.eventuate.server.common.PublishedEvent;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;

/**
 * Subscribe to domain events published to Kafka
 */
public class EventuateKafkaAggregateSubscriptions implements AggregateEvents {

  private static Logger logger = LoggerFactory.getLogger(EventuateKafkaAggregateSubscriptions.class);

  public EventuateKafkaAggregateSubscriptions() {
  }

  private final List<EventuateKafkaConsumer> consumers = new ArrayList<>();

  public void cleanUp() {
    synchronized (consumers) {
      consumers.stream().forEach(EventuateKafkaConsumer::stop);
    }
    logger.debug("Waiting for consumers to commit");
    try {
      TimeUnit.SECONDS.sleep(2);
    } catch (InterruptedException e) {
      logger.error("Error waiting", e);
    }
  }

  private void addConsumer(EventuateKafkaConsumer consumer) {
    synchronized (consumers) {
      consumers.add(consumer);
    }
  }

  @Override
  public CompletableFuture<?> subscribe(String subscriberId, Map<String, Set<String>> aggregatesAndEvents,
                                        SubscriberOptions subscriberOptions,
                                        Function<SerializedEvent, CompletableFuture<?>> handler) {

    List<String> topics = aggregatesAndEvents.keySet()
            .stream()
            .map(AggregateTopicMapping::aggregateTypeToTopic)
            .collect(toList());

    EventuateKafkaConsumer consumer = new EventuateKafkaConsumer(subscriberId, (record, callback) -> {
      SerializedEvent se = toSerializedEvent(record);
      if (aggregatesAndEvents.get(se.getEntityType()).contains(se.getEventType())) {
        handler.apply(se).whenComplete((result, t) -> {
          callback.accept(null, t);
        });
      } else {
        callback.accept(null, null);
      }

    }, topics);

    addConsumer(consumer);
    consumer.start();

    return CompletableFuture.completedFuture(null);

  }

  private SerializedEvent toSerializedEvent(ConsumerRecord<String, String> record) {
    PublishedEvent pe = JSonMapper.fromJson(record.value(), PublishedEvent.class);
    return new SerializedEvent(
            Int128.fromString(pe.getId()),
            pe.getEntityId(),
            pe.getEntityType(),
            pe.getEventData(),
            pe.getEventType(),
            record.partition(),
            record.offset(),
            EtopEventContext.make(pe.getId(), record.topic(), record.partition(), record.offset()),
            pe.getMetadata());
  }


}
