package com.networknt.eventuate.cdc.mysql.binlog;

import com.networknt.config.Config;
import com.networknt.eventuate.kafka.KafkaConfig;
import com.networknt.eventuate.server.common.BinlogFileOffset;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.util.Arrays;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;

public abstract class OffsetKafkaStore {

  protected final String dbHistoryTopicName;
  private static final String CONFIG_NAME = "kafka";
  private static final KafkaConfig config = (KafkaConfig) Config.getInstance().getJsonObjectConfig(CONFIG_NAME, KafkaConfig.class);

  private final static int N = 20;

  public OffsetKafkaStore(String dbHistoryTopicName) {
    this.dbHistoryTopicName = dbHistoryTopicName;
  }

  public Optional<BinlogFileOffset> getLastBinlogFileOffset() {
    try (KafkaConsumer<String, String> consumer = createConsumer()) {
      consumer.partitionsFor(dbHistoryTopicName);
      consumer.subscribe(Arrays.asList(dbHistoryTopicName));

      int count = N;
      BinlogFileOffset result = null;
      boolean lastRecordFound = false;
      while (!lastRecordFound) {
        ConsumerRecords<String, String> records = consumer.poll(100);
        if (records.isEmpty()) {
          count--;
          if (count == 0)
            lastRecordFound = true;
        } else {
          count = N;
          for (ConsumerRecord<String, String> record : records) {
            BinlogFileOffset current = handleRecord(record);
            if (current != null) {
              result = current;
            }
          }
        }
      }
      return Optional.ofNullable(result);
    }
  }

  private KafkaConsumer<String, String> createConsumer() {
    Properties props = new Properties();
    props.put("bootstrap.servers", config.getBootstrapServers());
    props.put("auto.offset.reset", "earliest");
    props.put("group.id", UUID.randomUUID().toString());
    props.put("enable.auto.commit", "false");
    props.put("auto.commit.interval.ms", "1000");
    props.put("session.timeout.ms", "30000");
    props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
    props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

    return new KafkaConsumer<>(props);
  }

  protected abstract BinlogFileOffset handleRecord(ConsumerRecord<String, String> record);
}
