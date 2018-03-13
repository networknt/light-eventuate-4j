package com.networknt.eventuate.server.test.util;

import com.networknt.config.Config;
import com.networknt.eventuate.common.impl.EntityIdVersionAndEventIds;
import com.networknt.eventuate.jdbc.EventuateJdbcAccess;
import com.networknt.eventuate.kafka.KafkaConfig;
import com.networknt.eventuate.server.common.CdcKafkaPublisher;
import com.networknt.eventuate.server.common.CdcProcessor;
import com.networknt.eventuate.server.common.PublishedEvent;
import com.networknt.eventuate.server.common.PublishingStrategy;
import com.networknt.eventuate.server.common.exception.EventuateLocalPublishingException;
import com.networknt.eventuate.server.jdbckafkastore.EventuateLocalAggregateCrud;
import com.networknt.service.SingletonServiceFactory;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.Collections;


public abstract class CdcKafkaPublisherTest extends AbstractCdcTest {

  private static final String CONFIG_NAME = "kafka";
  private static final KafkaConfig config = (KafkaConfig) Config.getInstance().getJsonObjectConfig(CONFIG_NAME, KafkaConfig.class);

  protected EventuateJdbcAccess eventuateJdbcAccess = SingletonServiceFactory.getBean(EventuateJdbcAccess.class);

  protected CdcProcessor<PublishedEvent> cdcProcessor = SingletonServiceFactory.getBean(CdcProcessor.class);

  protected PublishingStrategy<PublishedEvent> publishingStrategy = SingletonServiceFactory.getBean(PublishingStrategy.class);

  protected EventuateLocalAggregateCrud localAggregateCrud;

  @Before
  public void init() {
    localAggregateCrud = new EventuateLocalAggregateCrud(eventuateJdbcAccess);
  }

  @Test
  public void shouldSendPublishedEventsToKafka() throws InterruptedException {
    CdcKafkaPublisher<PublishedEvent> cdcKafkaPublisher = createCdcKafkaPublisher();

    cdcKafkaPublisher.start();

    cdcProcessor.start(cdcKafkaPublisher::handleEvent);

    String accountCreatedEventData = generateAccountCreatedEvent();
    EntityIdVersionAndEventIds entityIdVersionAndEventIds = saveEvent(localAggregateCrud, accountCreatedEventData);

    KafkaConsumer<String, String> consumer = createConsumer(config.getBootstrapServers());
    consumer.partitionsFor(getEventTopicName());
    consumer.subscribe(Collections.singletonList(getEventTopicName()));

    waitForEventInKafka(consumer, entityIdVersionAndEventIds.getEntityId(), LocalDateTime.now().plusSeconds(20));
    cdcKafkaPublisher.stop();
  }

  protected abstract CdcKafkaPublisher<PublishedEvent> createCdcKafkaPublisher();
}
