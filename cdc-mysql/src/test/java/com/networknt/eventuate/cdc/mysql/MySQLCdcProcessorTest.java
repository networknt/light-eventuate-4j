package com.networknt.eventuate.cdc.mysql;

import com.networknt.eventuate.server.common.PublishedEvent;
import com.networknt.eventuate.common.Int128;
import com.networknt.eventuate.common.impl.EntityIdVersionAndEventIds;
import com.networknt.eventuate.jdbc.EventuateLocalAggregateStore;
import com.networknt.eventuate.kafka.producer.EventuateKafkaProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * This test relies on Mysql, Zookeeper and Kafka running. All tests are disabled
 * unless you are working on the CDC.
 */
public class MySQLCdcProcessorTest extends AbstractCdcTest {

  private MySqlBinaryLogClient<PublishedEvent> mySqlBinaryLogClient;
  private DatabaseBinlogOffsetKafkaStore binlogOffsetKafkaStore;

  EventuateLocalAggregateStore localAggregateCrud;

  private Logger logger = LoggerFactory.getLogger(this.getClass());

  //@Before
  public void init() {

    SourceTableNameSupplier supplier = new SourceTableNameSupplier(cdcConfig.getSourceTableName(), "EVENTS");
    IWriteRowsEventDataParser eventDataParser = new WriteRowsEventDataParser(dataSource, supplier.getSourceTableName());
    mySqlBinaryLogClient = new MySqlBinaryLogClient<>(
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

    localAggregateCrud = new EventuateLocalAggregateStore(dataSource);
  }

  //@Test
  public void shouldReadNewEventsOnly() throws InterruptedException {
    BlockingQueue<PublishedEvent> publishedEvents = new LinkedBlockingDeque<>();
    MySQLCdcProcessor<PublishedEvent> mySQLCdcProcessor = new MySQLCdcProcessor<>(mySqlBinaryLogClient, binlogOffsetKafkaStore);
    mySQLCdcProcessor.start(publishedEvent -> {
      publishedEvents.add(publishedEvent);
      binlogOffsetKafkaStore.save(publishedEvent.getBinlogFileOffset());
    });

    String accountCreatedEventData = generateAccountCreatedEvent();
    EntityIdVersionAndEventIds entityIdVersionAndEventIds = saveEvent(localAggregateCrud, accountCreatedEventData);
    waitForEvent(publishedEvents, entityIdVersionAndEventIds.getEntityVersion(), LocalDateTime.now().plusSeconds(10), accountCreatedEventData);
    mySQLCdcProcessor.stop();

    publishedEvents.clear();
    mySQLCdcProcessor.start(publishedEvent -> {
      publishedEvents.add(publishedEvent);
      binlogOffsetKafkaStore.save(publishedEvent.getBinlogFileOffset());
    });
    List<String> excludedIds = entityIdVersionAndEventIds.getEventIds().stream().map(Int128::asString).collect(Collectors.toList());

    accountCreatedEventData = generateAccountCreatedEvent();
    entityIdVersionAndEventIds = saveEvent(localAggregateCrud, accountCreatedEventData);
    waitForEventExcluding(publishedEvents, entityIdVersionAndEventIds.getEntityVersion(), LocalDateTime.now().plusSeconds(10), accountCreatedEventData, excludedIds);
    mySQLCdcProcessor.stop();
  }

  //@Test
  public void shouldReadUnprocessedEventsAfterStartup() throws InterruptedException {
    BlockingQueue<PublishedEvent> publishedEvents = new LinkedBlockingDeque<>();

    String accountCreatedEventData = generateAccountCreatedEvent();
    EntityIdVersionAndEventIds entityIdVersionAndEventIds = saveEvent(localAggregateCrud, accountCreatedEventData);

    MySQLCdcProcessor<PublishedEvent> mySQLCdcProcessor = new MySQLCdcProcessor<>(mySqlBinaryLogClient, binlogOffsetKafkaStore);
    mySQLCdcProcessor.start(publishedEvents::add);

    waitForEvent(publishedEvents, entityIdVersionAndEventIds.getEntityVersion(), LocalDateTime.now().plusSeconds(20), accountCreatedEventData);
    mySQLCdcProcessor.stop();
  }

  private PublishedEvent waitForEventExcluding(BlockingQueue<PublishedEvent> publishedEvents, Int128 eventId, LocalDateTime deadline, String eventData, List<String> excludedIds) throws InterruptedException {
    PublishedEvent result = null;
    while (LocalDateTime.now().isBefore(deadline)) {
      long millis = ChronoUnit.MILLIS.between(deadline, LocalDateTime.now());
      PublishedEvent event = publishedEvents.poll(millis, TimeUnit.MILLISECONDS);
      if (event != null) {
        if (event.getId().equals(eventId.asString()) && eventData.equals(event.getEventData()))
          result = event;
        if (excludedIds.contains(event.getId()))
          throw new RuntimeException("Wrong event found in the queue");
      }
    }
    if (result != null)
      return result;
    throw new RuntimeException("event not found: " + eventId);
  }
}
