package com.networknt.eventuate.cdc.polling;

import com.networknt.eventuate.jdbc.client.EventuateLocalAggregateCrud;
import com.networknt.eventuate.server.common.CdcKafkaPublisher;
import com.networknt.eventuate.server.common.PublishedEvent;
import com.networknt.eventuate.server.test.util.CdcKafkaPublisherTest;
import org.junit.Before;

public class PollingCdcKafkaPublisherT extends CdcKafkaPublisherTest {

  @Before
  public void init() {
    localAggregateCrud = new EventuateLocalAggregateCrud(eventuateJdbcAccess);
  }

  @Override
  protected CdcKafkaPublisher<PublishedEvent> createCdcKafkaPublisher() {
    return new PollingCdcKafkaPublisher<>(kafkaConfig.getBootstrapServers(), publishingStrategy);
  }
}
