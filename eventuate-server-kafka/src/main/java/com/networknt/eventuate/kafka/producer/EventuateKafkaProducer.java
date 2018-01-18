package com.networknt.eventuate.kafka.producer;

import com.networknt.config.Config;
import com.networknt.eventuate.kafka.KafkaConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * A Kafka Producer that send messages to defined Kafka instance
 *
 * *@param bootstrapServers Kafka bootstrap Servers string
 */
public class EventuateKafkaProducer {

  private Producer<String, String> producer;
  private Properties producerProps;

  static final KafkaConfig config = (KafkaConfig) Config.getInstance().getJsonObjectConfig(KafkaConfig.CONFIG_NAME, KafkaConfig.class);

  public EventuateKafkaProducer() {
    producerProps = new Properties();
    producerProps.put("bootstrap.servers", config.getBootstrapServers());
    producerProps.put("acks", config.getAcks());
    producerProps.put("retries", config.getRetries());
    producerProps.put("batch.size", config.getBatchSize());
    producerProps.put("linger.ms", config.getLingerms());
    producerProps.put("buffer.memory", config.getBufferMemory());
    producerProps.put("key.serializer", config.getKeySerializer());
    producerProps.put("value.serializer", config.getValueSerializer());
    producer = new KafkaProducer<>(producerProps);
  }


  public void setProducer (Producer<String, String> producer) {
    this.producer = producer;
  }

  /**
   * Update the aggregate
   * @param topic Kafka topic
   * @param key key the message in the topic
   * @param body message body
   * @return the CompletableFuture, which process asynchronous
   */
  public CompletableFuture<?> send(String topic, String key, String body) {
    CompletableFuture<Object> result = new CompletableFuture<>();
    producer.send(new ProducerRecord<>(topic, key, body), (metadata, exception) -> {
      if (exception == null)
        result.complete(metadata);
      else
        result.completeExceptionally(exception);
    });
    return result;
  }

  public void close() {
    producer.close(1, TimeUnit.SECONDS);
  }
}
