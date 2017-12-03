package com.networknt.eventuate.cdc.mysql;


import com.networknt.eventuate.server.common.CdcKafkaPublisher;
import com.networknt.eventuate.server.common.PublishedEvent;
import com.networknt.eventuate.server.test.util.CdcKafkaPublisherTest;

/**
 * This test relies on Mysql, Zookeeper and Kafka running. All tests are disabled
 * unless you are working on the CDC.
 */
public class MySQLCdcKafkaPublisherIT extends CdcKafkaPublisherTest {

  private DatabaseBinlogOffsetKafkaStore binlogOffsetKafkaStore;

  @Override
  protected CdcKafkaPublisher<PublishedEvent> createCdcKafkaPublisher() {
    return new MySQLCdcKafkaPublisher<>(binlogOffsetKafkaStore,
            kafkaConfig.getBootstrapServers(),
            publishingStrategy);
  }

}
