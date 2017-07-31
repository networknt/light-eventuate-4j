package com.networknt.eventuate.cdc.mysql;

import com.networknt.eventuate.cdc.common.PublishedEvent;
import com.networknt.eventuate.cdc.mysql.exception.EventuateLocalPublishingException;
import com.networknt.eventuate.common.impl.EntityIdVersionAndEventIds;
import com.networknt.eventuate.jdbc.EventuateLocalAggregateStore;
import com.networknt.eventuate.kafka.producer.EventuateKafkaProducer;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.Collections;

/**
 * This test relies on Mysql, Zookeeper and Kafka running. All tests are disabled
 * unless you are working on the CDC.
 */
public class MySQLCdcKafkaPublisherTest extends AbstractCdcTest {

  private DatabaseBinlogOffsetKafkaStore binlogOffsetKafkaStore;

  private PublishingStrategy<PublishedEvent> publishingStrategy;

  private EventuateLocalAggregateStore localAggregateCrud;

  private MySQLCdcProcessor<PublishedEvent> mySQLCdcProcessor;

  //@Before
  public void init() {

    SourceTableNameSupplier supplier = new SourceTableNameSupplier(cdcConfig.getSourceTableName(), "EVENTS");
    IWriteRowsEventDataParser eventDataParser = new WriteRowsEventDataParser(dataSource, supplier.getSourceTableName());
    MySqlBinaryLogClient<PublishedEvent> mySqlBinaryLogClient = new MySqlBinaryLogClient<>(
            eventDataParser,
            cdcConfig.getDbUser(),
            cdcConfig.getDbPass(),
            cdcConfig.getDbHost(),
            cdcConfig.getDbPort(),
            cdcConfig.getBinlogClientId(),
            supplier.getSourceTableName());

    EventuateKafkaProducer eventuateKafkaProducer = new EventuateKafkaProducer(kafkaConfig.getBootstrapServers());
    binlogOffsetKafkaStore = new DatabaseBinlogOffsetKafkaStore(
            cdcConfig.getDbHistoryTopicName(), mySqlBinaryLogClient.getName(), eventuateKafkaProducer);
    publishingStrategy = new PublishedEventPublishingStrategy();
    localAggregateCrud = new EventuateLocalAggregateStore(dataSource);
    mySQLCdcProcessor = new MySQLCdcProcessor<>(mySqlBinaryLogClient, binlogOffsetKafkaStore);
  }


  //@Test
  public void shouldSendPublishedEventsToKafka() throws InterruptedException {
    MySQLCdcKafkaPublisher<PublishedEvent> mySQLCdcKafkaPublisher = new MySQLCdcKafkaPublisher<>(binlogOffsetKafkaStore,
            kafkaConfig.getBootstrapServers(),
            publishingStrategy);
    mySQLCdcKafkaPublisher.start();

    mySQLCdcProcessor.start(publishedEvent -> {
      try {
        mySQLCdcKafkaPublisher.handleEvent(publishedEvent);
      } catch (EventuateLocalPublishingException e) {
        throw new RuntimeException(e);
      }
    });

    String accountCreatedEventData = generateAccountCreatedEvent();
    EntityIdVersionAndEventIds entityIdVersionAndEventIds = saveEvent(localAggregateCrud, accountCreatedEventData);

    KafkaConsumer<String, String> consumer = createConsumer(kafkaConfig.getBootstrapServers());
    consumer.partitionsFor(getEventTopicName());
    consumer.subscribe(Collections.singletonList(getEventTopicName()));

    waitForEventInKafka(consumer, entityIdVersionAndEventIds.getEntityId(), LocalDateTime.now().plusSeconds(20));
    mySQLCdcKafkaPublisher.stop();
  }

}
