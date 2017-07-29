package com.networknt.eventuate.cdc.mysql;

import com.networknt.eventuate.cdc.common.BinlogFileOffset;
import com.networknt.eventuate.kafka.producer.EventuateKafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class DatabaseBinlogOffsetKafkaStoreTest extends AbstractCdcTest{

  EventuateKafkaProducer eventuateKafkaProducer;

  @Before
  public void init() {
    eventuateKafkaProducer = new EventuateKafkaProducer(kafkaConfig.getBootstrapServers());
  }

  @Test
  public void shouldSendBinlogFilenameAndOffset() throws InterruptedException {
    generateAndSaveBinlogFileOffset();
  }

  @Test
  public void shouldGetEmptyOptionalFromEmptyTopic() {
    DatabaseBinlogOffsetKafkaStore binlogOffsetKafkaStore = getDatabaseBinlogOffsetKafkaStore(UUID.randomUUID().toString(), "mySqlBinaryLogClientName");
    assertFalse(binlogOffsetKafkaStore.getLastBinlogFileOffset().isPresent());
    binlogOffsetKafkaStore.stop();
  }

  @Test
  public void shouldWorkCorrectlyWithMultipleDifferentNamedBinlogs() throws InterruptedException {
    floodTopic(cdcConfig.getDbHistoryTopicName(), "mySqlBinaryLogClientName1");

    generateAndSaveBinlogFileOffset();
  }

  @Test
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
