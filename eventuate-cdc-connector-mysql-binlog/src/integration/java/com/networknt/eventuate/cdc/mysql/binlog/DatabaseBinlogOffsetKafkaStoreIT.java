package com.networknt.eventuate.cdc.mysql.binlog;

import com.networknt.eventuate.server.common.BinlogFileOffset;
import com.networknt.eventuate.kafka.producer.EventuateKafkaProducer;
import com.networknt.eventuate.server.test.util.AbstractCdcTest;
import com.networknt.service.SingletonServiceFactory;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.Ignore;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * This test relies on Mysql, Zookeeper and Kafka running. All tests are disabled
 * unless you are working on the CDC.
 */

public class DatabaseBinlogOffsetKafkaStoreIT extends AbstractCdcTest {

  EventuateKafkaProducer eventuateKafkaProducer = SingletonServiceFactory.getBean(EventuateKafkaProducer.class);

  @Test
  @Ignore
  public void shouldSendBinlogFilenameAndOffset() throws InterruptedException {
    generateAndSaveBinlogFileOffset();
  }

  @Test
  @Ignore
  public void shouldGetEmptyOptionalFromEmptyTopic() {
    DatabaseBinlogOffsetKafkaStore binlogOffsetKafkaStore = getDatabaseBinlogOffsetKafkaStore(UUID.randomUUID().toString(), "mySqlBinaryLogClientName");
    assertFalse(binlogOffsetKafkaStore.getLastBinlogFileOffset().isPresent());
    binlogOffsetKafkaStore.stop();
  }

  @Test
  @Ignore
  public void shouldWorkCorrectlyWithMultipleDifferentNamedBinlogs() throws InterruptedException {
    floodTopic(cdcConfig.getDbHistoryTopicName(), "mySqlBinaryLogClientName1");

    generateAndSaveBinlogFileOffset();
  }

  @Test
  @Ignore
  public void shouldReadTheLastRecordMultipleTimes() throws InterruptedException {
    BinlogFileOffset bfo = generateAndSaveBinlogFileOffset();

    assertLastRecordEquals(bfo);
    assertLastRecordEquals(bfo);
  }

  private void floodTopic(String topicName, String key) {
    Producer<String, String> producer = createProducer(kafkaConfig.getBootstrapServers());
    for (int i = 0; i < 10; i++)
      producer.send(new ProducerRecord<>(topicName, key, Integer.toString(i)));

    producer.close();
  }

  public DatabaseBinlogOffsetKafkaStore getDatabaseBinlogOffsetKafkaStore(String topicName, String key) {
    return new DatabaseBinlogOffsetKafkaStore(topicName, key, eventuateKafkaProducer);
  }

  private BinlogFileOffset generateAndSaveBinlogFileOffset() throws InterruptedException {
    BinlogFileOffset bfo = generateBinlogFileOffset();
    DatabaseBinlogOffsetKafkaStore binlogOffsetKafkaStore = getDatabaseBinlogOffsetKafkaStore(cdcConfig.getDbHistoryTopicName(), "mySqlBinaryLogClientName");
    binlogOffsetKafkaStore.save(bfo);

    Thread.sleep(5000);

    BinlogFileOffset savedBfo = binlogOffsetKafkaStore.getLastBinlogFileOffset().get();
    assertEquals(bfo, savedBfo);
    binlogOffsetKafkaStore.stop();
    return savedBfo;
  }

  private void assertLastRecordEquals(BinlogFileOffset binlogFileOffset) {
    DatabaseBinlogOffsetKafkaStore binlogOffsetKafkaStore = getDatabaseBinlogOffsetKafkaStore(cdcConfig.getDbHistoryTopicName(), "mySqlBinaryLogClientName");

    BinlogFileOffset lastRecord = binlogOffsetKafkaStore.getLastBinlogFileOffset().get();
    assertEquals(binlogFileOffset, lastRecord);
    binlogOffsetKafkaStore.stop();
  }
}
