package com.networknt.eventuate.cdccore.kafka.producer;

import com.networknt.config.Config;
import com.networknt.eventuate.cdccore.kafka.KafkaConfig;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class CdcKafkaProducer {

  private Producer<String, String> producer;
  private String bootstrapServers;
  private Properties producerProps;

  static String CONFIG_NAME = "kafkaconfig";
  static final KafkaConfig config = (KafkaConfig) Config.getInstance().getJsonObjectConfig(CONFIG_NAME, KafkaConfig.class);

  public CdcKafkaProducer(){
    this(config.getBootstrapServers());
  }

  public CdcKafkaProducer(String bootstrapServers) {
    this.bootstrapServers = bootstrapServers;
    producerProps = new Properties();
    producerProps.put("bootstrap.servers", bootstrapServers);
    producerProps.put("acks", config.getAcks());
    producerProps.put("retries", config.getRetries());
    producerProps.put("batch.size", config.getBatchSize());
    producerProps.put("linger.ms", config.getLingerms());
    producerProps.put("buffer.memory", config.getBufferMemory());
    producerProps.put("key.serializer", config.getKeySerializer());
    producerProps.put("value.serializer", config.getValueSerializer());
    producer = new org.apache.kafka.clients.producer.KafkaProducer<>(producerProps);
  }

  public void setProducer (Producer<String, String> producer) {
    this.producer = producer;
  }

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
