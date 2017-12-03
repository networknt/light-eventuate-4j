package com.networknt.eventuate.cdc.mysql;

import com.networknt.eventuate.common.impl.EntityIdVersionAndEventIds;
import com.networknt.eventuate.jdbc.EventuateJdbcAccess;
import com.networknt.eventuate.server.common.PublishedEvent;
import com.networknt.eventuate.server.jdbckafkastore.EventuateLocalAggregateCrud;
import com.networknt.eventuate.server.test.util.AbstractCdcTest;
import org.junit.Test;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeoutException;

public abstract class AbstractMySqlBinlogCdcIntegrationIT extends AbstractCdcTest {

  private String dataSourceURL;

  EventuateJdbcAccess eventuateJdbcAccess;

  private WriteRowsEventDataParser eventDataParser;

  private SourceTableNameSupplier sourceTableNameSupplier;

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
    LocalDateTime deadline = LocalDateTime.now().plusSeconds(10);

    waitForEvent(publishedEvents, saveResult.getEntityVersion(), deadline, accountCreatedEventData);
    waitForEvent(publishedEvents, updateResult.getEntityVersion(), deadline, accountDebitedEventData);
    mySqlBinaryLogClient.stop();
  }

}
