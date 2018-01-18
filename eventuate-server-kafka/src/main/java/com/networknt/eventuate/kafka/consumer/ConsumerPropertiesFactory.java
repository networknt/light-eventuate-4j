package com.networknt.eventuate.kafka.consumer;

import com.networknt.eventuate.kafka.KafkaConfig;

import java.util.Properties;

public class ConsumerPropertiesFactory {
  public static Properties makeConsumerProperties(KafkaConfig config, String subscriberId) {
    Properties consumerProperties = new Properties();
    consumerProperties.put("bootstrap.servers", config.getBootstrapServers());
    consumerProperties.put("group.id", subscriberId);
    consumerProperties.put("enable.auto.commit", config.isEnableaAutocommit());
    consumerProperties.put("session.timeout.ms", config.getSessionTimeout());
    consumerProperties.put("key.deserializer", config.getKeyDeSerializer());
    consumerProperties.put("value.deserializer", config.getValueDeSerializer());
    consumerProperties.put("auto.offset.reset", config.getAutoOffsetreset());
    return consumerProperties;
  }
}
