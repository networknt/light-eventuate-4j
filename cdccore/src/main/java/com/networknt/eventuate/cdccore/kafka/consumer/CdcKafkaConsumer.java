package com.networknt.eventuate.cdccore.kafka.consumer;

import com.networknt.config.Config;
import com.networknt.eventuate.cdccore.kafka.KafkaConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

/**
 * A Kafka consumer that manually commits offsets and supports asynchronous message processing
 * *@param subscriberId message subscriber id
 * *@handler defined message handler
 * *@param topics Kafka topic list
 * *@param bootstrapServers Kafka bootstrap Servers string
 */
public class CdcKafkaConsumer {


  private final String subscriberId;
  private final BiConsumer<ConsumerRecord<String, String>, BiConsumer<Void, Throwable>> handler;
  private final List<String> topics;
  private Logger logger = LoggerFactory.getLogger(getClass());
  private AtomicBoolean stopFlag = new AtomicBoolean(false);
  private Properties consumerProperties;
  static String CONFIG_NAME = "kafkaconfig";
  static KafkaConfig config;

  public CdcKafkaConsumer(String subscriberId, BiConsumer<ConsumerRecord<String, String>, BiConsumer<Void, Throwable>> handler, List<String> topics, String bootstrapServers) {

    this.subscriberId = subscriberId;
    this.handler = handler;
    this.topics = topics;

    config = (KafkaConfig) Config.getInstance().getJsonObjectConfig(CONFIG_NAME, KafkaConfig.class);

    consumerProperties = new Properties();
    consumerProperties.put("bootstrap.servers", bootstrapServers);
    consumerProperties.put("group.id", subscriberId);
    consumerProperties.put("enable.auto.commit", config.isEnableaAutocommit());
    //consumerProperties.put("auto.commit.interval.ms", "1000");
    consumerProperties.put("session.timeout.ms", config.getSessionTimeout());
    consumerProperties.put("key.deserializer", config.getKeyDeSerializer());
    consumerProperties.put("value.deserializer", config.getValueDeSerializer());
    consumerProperties.put("auto.offset.reset", config.getAutoOffsetreset());
  }

  private void verifyTopicExistsBeforeSubscribing(org.apache.kafka.clients.consumer.KafkaConsumer<String, String> consumer, String topic) {
    try {
      logger.debug("Verifying Topic {}", topic);
      List<PartitionInfo> partitions = consumer.partitionsFor(topic);
      logger.debug("Got these partitions {} for Topic {}", partitions, topic);
    } catch (Throwable e) {
      logger.error("Got exception: ", e);
      throw new RuntimeException(e);
    }
  }

  private void maybeCommitOffsets(org.apache.kafka.clients.consumer.KafkaConsumer<String, String> consumer, KafkaMessageProcessor processor) {
    Map<TopicPartition, OffsetAndMetadata> offsetsToCommit = processor.offsetsToCommit();
    if (!offsetsToCommit.isEmpty()) {
      logger.debug("Committing offsets {} {}", subscriberId, offsetsToCommit);
      consumer.commitSync(offsetsToCommit);
      logger.debug("Committed offsets {}", subscriberId);
      processor.noteOffsetsCommitted(offsetsToCommit);
    }
  }

  /**
   * start Kafka consumer process
   */
  public void start() {
    try {

      org.apache.kafka.clients.consumer.KafkaConsumer<String, String> consumer = new org.apache.kafka.clients.consumer.KafkaConsumer<>(consumerProperties);

      KafkaMessageProcessor processor = new KafkaMessageProcessor(subscriberId, handler);

      for (String topic : topics) {
        verifyTopicExistsBeforeSubscribing(consumer, topic);
      }

      logger.debug("Subscribing to {} {}", subscriberId, topics);

      consumer.subscribe(new ArrayList<>(topics));

      logger.debug("Subscribed to {} {}", subscriberId, topics);

      new Thread(() -> {


        try {
          while (!stopFlag.get()) {
            ConsumerRecords<String, String> records = consumer.poll(100);
            if (!records.isEmpty())
              logger.debug("Got {} {} records", subscriberId, records.count());

            for (ConsumerRecord<String, String> record : records) {
              logger.debug("processing record {} {} {}", subscriberId, record.offset(), record.value());
              if (logger.isDebugEnabled())
                logger.debug(String.format("EventuateKafkaAggregateSubscriptions subscriber = %s, offset = %d, key = %s, value = %s", subscriberId, record.offset(), record.key(), record.value()));
              if ( record !=null &&  record.key() !=null &&  record.value()!=null) {
                processor.process(record);
              }
            }
            if (!records.isEmpty())
              logger.debug("Processed {} {} records", subscriberId, records.count());

            maybeCommitOffsets(consumer, processor);

            if (!records.isEmpty())
              logger.debug("To commit {} {}", subscriberId, processor.getPending());

          }

          maybeCommitOffsets(consumer, processor);

        } catch (Throwable e) {
          logger.error("Got exception: ", e);
          throw new RuntimeException(e);
        }

      }, "Eventuate-subscriber-" + subscriberId).start();

    } catch (Exception e) {
      logger.error("Error subscribing", e);
      throw new RuntimeException(e);
    }
  }

  public void stop() {
    stopFlag.set(true);
  }

}
