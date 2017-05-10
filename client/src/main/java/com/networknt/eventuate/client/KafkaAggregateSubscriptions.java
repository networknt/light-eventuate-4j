package com.networknt.eventuate.client;


import com.fasterxml.jackson.databind.ObjectMapper;

import com.networknt.eventuate.cdccore.AggregateTopicMapping;
import com.networknt.eventuate.cdccore.PublishedEvent;
import com.networknt.eventuate.cdccore.kafka.consumer.CdcKafkaConsumer;
import com.networknt.eventuate.common.EventContext;
import com.networknt.eventuate.common.Int128;
import com.networknt.eventuate.common.SubscriberOptions;
import com.networknt.eventuate.common.impl.AggregateEvents;
import com.networknt.eventuate.common.impl.SerializedEvent;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import java.io.IOException;
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
 *  * @param bootstrapServers Kafka bootstrap Servers string
 */
public class KafkaAggregateSubscriptions implements AggregateEvents {

  private Logger logger = LoggerFactory.getLogger(getClass());

  String bootstrapServers;

  public KafkaAggregateSubscriptions(String bootstrapServers) {
    this.bootstrapServers = bootstrapServers;
  }

  private final List<CdcKafkaConsumer> consumers = new ArrayList<>();

  @PreDestroy
  public void cleanUp() {
    synchronized (consumers) {
      consumers.stream().forEach(CdcKafkaConsumer::stop);
    }
    logger.debug("Waiting for consumers to commit");
    try {
      TimeUnit.SECONDS.sleep(2);
    } catch (InterruptedException e) {
      logger.error("Error waiting", e);
    }
  }

  private void addConsumer(CdcKafkaConsumer consumer) {
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

    CdcKafkaConsumer consumer = new CdcKafkaConsumer(subscriberId, (record, callback) -> {
      SerializedEvent se = toSerializedEvent(record);
      if (aggregatesAndEvents.get(se.getEntityType()).contains(se.getEventType())) {
        handler.apply(se).whenComplete((result, t) -> {
          callback.accept(null, t);
        });
      } else {
        callback.accept(null, null);
      }

    }, topics, bootstrapServers);

    addConsumer(consumer);
    consumer.start();

    return CompletableFuture.completedFuture(null);

  }

  private SerializedEvent toSerializedEvent(ConsumerRecord<String, String> record) {
    try {
      ObjectMapper om = new ObjectMapper();
      PublishedEvent pe = om.readValue(record.value(), PublishedEvent.class);
      return new SerializedEvent(
              Int128.fromString(pe.getId()),
              pe.getEntityId(),
              pe.getEntityType(),
              pe.getEventData(),
              pe.getEventType(),
              record.partition(),
              record.offset(),
              new EventContext(makeEventToken(pe.getId(), record.topic(), record.partition(), record.offset())));
    } catch (IOException e) {
      logger.error("Got exception: ", e);
      throw new RuntimeException(e);
    }
  }

  private String makeEventToken(String id, String topic, int partition, long offset) {
    return id;
  }


}
