package com.networknt.eventuate.cdc.mysql.binlog;


import com.networknt.eventuate.server.common.CdcKafkaPublisher;
import com.networknt.eventuate.server.common.PublishedEvent;
import com.networknt.eventuate.server.test.util.CdcKafkaPublisherTest;
import com.networknt.service.SingletonServiceFactory;

import javax.xml.crypto.Data;

/**
 * This test relies on Mysql, Zookeeper and Kafka running. All tests are disabled
 * unless you are working on the CDC.
 */
public class MySQLCdcKafkaPublisherIT extends CdcKafkaPublisherTest {

  private DatabaseBinlogOffsetKafkaStore binlogOffsetKafkaStore = SingletonServiceFactory.getBean(DatabaseBinlogOffsetKafkaStore.class);

  @Override
  protected CdcKafkaPublisher<PublishedEvent> createCdcKafkaPublisher() {
    return new MySQLCdcKafkaPublisher<>(binlogOffsetKafkaStore,
            kafkaConfig.getBootstrapServers(),
            publishingStrategy);
  }

}
