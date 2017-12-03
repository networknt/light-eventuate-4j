package com.networknt.eventuate.cdc.mysql;

import com.networknt.eventuate.server.common.BinlogFileOffset;
import com.networknt.eventuate.server.common.PublishedEvent;
import com.networknt.eventuate.common.impl.JSonMapper;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.UUID;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * This test relies on Mysql, Zookeeper and Kafka running. All tests are disabled
 * unless you are working on the CDC.
 */
public class DuplicatePublishingDetectorTest extends AbstractCdcTest {

  //@Test
  public void emptyTopicTest() {
    DuplicatePublishingDetector duplicatePublishingDetector = new DuplicatePublishingDetector(kafkaConfig.getBootstrapServers());

    BinlogFileOffset bfo = generateBinlogFileOffset();

    assertTrue(duplicatePublishingDetector.shouldBePublished(bfo, generateUniqueTopicName()));
  }

  //@Test
  public void shouldBePublishedTest() {
    String topicName = generateUniqueTopicName();
    String binlogFilename = "binlog.file." + System.currentTimeMillis();
    DuplicatePublishingDetector duplicatePublishingDetector = new DuplicatePublishingDetector(kafkaConfig.getBootstrapServers());

    Producer<String, String> producer = createProducer(kafkaConfig.getBootstrapServers());
    floodTopic(producer, binlogFilename, topicName);
    producer.close();

    assertFalse(duplicatePublishingDetector.shouldBePublished(new BinlogFileOffset(binlogFilename, 1), topicName));
    assertTrue(duplicatePublishingDetector.shouldBePublished(new BinlogFileOffset(binlogFilename, 10), topicName));
  }

  //@Test
  public void shouldHandlePublishCheckForOldEntires() {
    String topicName = generateUniqueTopicName();
    String binlogFilename = "binlog.file." + System.currentTimeMillis();
    DuplicatePublishingDetector duplicatePublishingDetector = new DuplicatePublishingDetector(kafkaConfig.getBootstrapServers());

    Producer<String, String> producer = createProducer(kafkaConfig.getBootstrapServers());
    floodTopic(producer, binlogFilename, topicName);
    sendOldPublishedEvent(producer, topicName);
    producer.close();

    assertTrue(duplicatePublishingDetector.shouldBePublished(new BinlogFileOffset(binlogFilename, 10), topicName));
  }

  private void floodTopic(Producer<String, String> producer, String binlogFilename, String topicName) {
    for (int i = 0; i < 10; i++) {
      PublishedEvent publishedEvent = new PublishedEvent();
      publishedEvent.setEntityId(UUID.randomUUID().toString());
      publishedEvent.setBinlogFileOffset(new BinlogFileOffset(binlogFilename, i));
      String json = JSonMapper.toJson(publishedEvent);
      producer.send(
              new ProducerRecord<>(topicName,
                      publishedEvent.getEntityId(),
                      json));

    }

  }

  private void sendOldPublishedEvent(Producer<String, String> producer, String topicName) {
    for (int i = 0; i < 10; i++) {
      PublishedEvent publishedEvent = new PublishedEvent();
      publishedEvent.setEntityId(UUID.randomUUID().toString());
      String json = JSonMapper.toJson(publishedEvent);
      producer.send(
              new ProducerRecord<>(topicName,
                      publishedEvent.getEntityId(),
                      json));
    }
  }

}
