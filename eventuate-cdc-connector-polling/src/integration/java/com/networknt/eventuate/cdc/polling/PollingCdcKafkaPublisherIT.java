package com.networknt.eventuate.cdc.polling;

import com.networknt.eventuate.server.common.CdcKafkaPublisher;
import com.networknt.eventuate.server.common.PublishedEvent;
import com.networknt.eventuate.server.jdbckafkastore.EventuateLocalAggregateCrud;
import com.networknt.eventuate.server.test.util.CdcKafkaPublisherTest;
import org.junit.Before;

public class PollingCdcKafkaPublisherIT extends CdcKafkaPublisherTest {

  @Before
  public void init() {
    localAggregateCrud = new EventuateLocalAggregateCrud(eventuateJdbcAccess);
  }

  @Override
  protected CdcKafkaPublisher<PublishedEvent> createCdcKafkaPublisher() {
    return new PollingCdcKafkaPublisher<>(kafkaConfig.getBootstrapServers(), publishingStrategy);
  }
}
