package com.networknt.eventuate.cdc.mysql.binlog;

import com.networknt.eventuate.common.impl.EntityIdVersionAndEventIds;

import com.networknt.eventuate.jdbc.client.EventuateLocalAggregateCrud;
import com.networknt.eventuate.jdbc.common.EventuateJdbcAccess;
import com.networknt.eventuate.server.common.PublishedEvent;

import com.networknt.eventuate.server.test.util.AbstractCdcTest;
import com.networknt.service.SingletonServiceFactory;
import org.junit.Test;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeoutException;

public abstract class AbstractMySqlBinlogCdcIntegrationIT extends AbstractCdcTest {

  EventuateJdbcAccess eventuateJdbcAccess = SingletonServiceFactory.getBean(EventuateJdbcAccess.class);

  private IWriteRowsEventDataParser eventDataParser = SingletonServiceFactory.getBean(IWriteRowsEventDataParser.class);

  private SourceTableNameSupplier sourceTableNameSupplier = SingletonServiceFactory.getBean(SourceTableNameSupplier.class);

  @Test
  public void shouldGetEvents() throws IOException, TimeoutException, InterruptedException, ExecutionException {
    MySqlBinaryLogClient<PublishedEvent> mySqlBinaryLogClient = new MySqlBinaryLogClient<>(eventDataParser,
            cdcConfig.getDbUser(),
            cdcConfig.getDbPass(),
            cdcConfig.getDbHost(),
            cdcConfig.getDbPort(),
            cdcConfig.getBinlogClientId(),
            sourceTableNameSupplier.getSourceTableName(),
            cdcConfig.getMySqlBinLogClientName());

    EventuateLocalAggregateCrud localAggregateCrud = new EventuateLocalAggregateCrud(eventuateJdbcAccess);

    BlockingQueue<PublishedEvent> publishedEvents = new LinkedBlockingDeque<>();

    mySqlBinaryLogClient.start(Optional.empty(), publishedEvents::add);
    String accountCreatedEventData = generateAccountCreatedEvent();
    EntityIdVersionAndEventIds saveResult = saveEvent(localAggregateCrud, accountCreatedEventData);

    String accountDebitedEventData = generateAccountDebitedEvent();
    EntityIdVersionAndEventIds updateResult = updateEvent(saveResult.getEntityId(), saveResult.getEntityVersion(), localAggregateCrud, accountDebitedEventData);

    // Wait for 10 seconds
    LocalDateTime deadline = LocalDateTime.now().plusSeconds(20);

    waitForEvent(publishedEvents, saveResult.getEntityVersion(), deadline, accountCreatedEventData);
    waitForEvent(publishedEvents, updateResult.getEntityVersion(), deadline, accountDebitedEventData);
    mySqlBinaryLogClient.stop();
  }

}
