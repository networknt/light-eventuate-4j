package com.networknt.eventuate.cdc.mysql;

import com.networknt.eventuate.cdc.common.PublishedEvent;
import com.networknt.eventuate.common.impl.EntityIdVersionAndEventIds;
import com.networknt.eventuate.jdbc.EventuateLocalAggregateStore;
import org.junit.Test;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeoutException;

/**
 * This test relies on Mysql, Zookeeper and Kafka running. All tests are disabled
 * unless you are working on the CDC.
 */
public class MySqlBinlogCdcIntegrationTest extends AbstractCdcTest {

  //@Test
  public void shouldGetEvents() throws IOException, TimeoutException, InterruptedException, ExecutionException {

    SourceTableNameSupplier supplier = new SourceTableNameSupplier(cdcConfig.getSourceTableName(), "EVENTS");

    IWriteRowsEventDataParser dataParser = new WriteRowsEventDataParser(dataSource, supplier.getSourceTableName());
    MySqlBinaryLogClient<PublishedEvent> mySqlBinaryLogClient = new MySqlBinaryLogClient<>(dataParser,
            cdcConfig.getDbUser(),
            cdcConfig.getDbPass(),
            cdcConfig.getDbHost(),
            cdcConfig.getDbPort(),
            cdcConfig.getBinlogClientId(),
            supplier.getSourceTableName());

    EventuateLocalAggregateStore localAggregateCrud = new EventuateLocalAggregateStore(dataSource);

    BlockingQueue<PublishedEvent> publishedEvents = new LinkedBlockingDeque<>();

    mySqlBinaryLogClient.start(Optional.empty(), publishedEvents::add);
    String accountCreatedEventData = generateAccountCreatedEvent();
    EntityIdVersionAndEventIds saveResult = saveEvent(localAggregateCrud, accountCreatedEventData);

    String accountDebitedEventData = generateAccountDebitedEvent();
    EntityIdVersionAndEventIds updateResult = updateEvent(saveResult.getEntityId(), saveResult.getEntityVersion(), localAggregateCrud, accountDebitedEventData);

    // Wait for 10 seconds
    LocalDateTime deadline = LocalDateTime.now().plusSeconds(10);

    waitForEvent(publishedEvents, saveResult.getEntityVersion(), deadline, accountCreatedEventData);
    waitForEvent(publishedEvents, updateResult.getEntityVersion(), deadline, accountDebitedEventData);
    mySqlBinaryLogClient.stop();
  }

}
