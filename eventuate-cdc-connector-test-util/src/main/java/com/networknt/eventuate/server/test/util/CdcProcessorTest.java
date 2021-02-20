package com.networknt.eventuate.server.test.util;

import com.networknt.eventuate.common.Int128;
import com.networknt.eventuate.common.impl.EntityIdVersionAndEventIds;
import com.networknt.eventuate.jdbc.EventuateJdbcAccess;
import com.networknt.eventuate.server.common.CdcProcessor;
import com.networknt.eventuate.server.common.PublishedEvent;
import com.networknt.eventuate.server.jdbckafkastore.EventuateLocalAggregateCrud;
import com.networknt.service.SingletonServiceFactory;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.swing.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public abstract class CdcProcessorTest extends AbstractCdcTest {

  protected EventuateJdbcAccess eventuateJdbcAccess = SingletonServiceFactory.getBean(EventuateJdbcAccess.class);

  protected EventuateLocalAggregateCrud localAggregateCrud;

  @Before
  public void init() {
    localAggregateCrud = new EventuateLocalAggregateCrud(eventuateJdbcAccess);
  }

  @Test
  @Ignore
  public void shouldReadNewEventsOnly() throws InterruptedException {
    BlockingQueue<PublishedEvent> publishedEvents = new LinkedBlockingDeque<>();
    CdcProcessor<PublishedEvent> cdcProcessor = createCdcProcessor();
    cdcProcessor.start(publishedEvent -> {
      publishedEvents.add(publishedEvent);
      onEventSent(publishedEvent);
    });

    String accountCreatedEventData = generateAccountCreatedEvent();
    EntityIdVersionAndEventIds entityIdVersionAndEventIds = saveEvent(localAggregateCrud, accountCreatedEventData);
    waitForEvent(publishedEvents, entityIdVersionAndEventIds.getEntityVersion(), LocalDateTime.now().plusSeconds(10), accountCreatedEventData);
    cdcProcessor.stop();

    Thread.sleep(10000);

    publishedEvents.clear();
    cdcProcessor.start(publishedEvent -> {
      publishedEvents.add(publishedEvent);
      onEventSent(publishedEvent);
    });
    List<String> excludedIds = entityIdVersionAndEventIds.getEventIds().stream().map(Int128::asString).collect(Collectors.toList());

    accountCreatedEventData = generateAccountCreatedEvent();
    entityIdVersionAndEventIds = saveEvent(localAggregateCrud, accountCreatedEventData);
    waitForEventExcluding(publishedEvents, entityIdVersionAndEventIds.getEntityVersion(), LocalDateTime.now().plusSeconds(10), accountCreatedEventData, excludedIds);
    cdcProcessor.stop();
  }

  @Test
  @Ignore
  public void shouldReadUnprocessedEventsAfterStartup() throws InterruptedException {
    BlockingQueue<PublishedEvent> publishedEvents = new LinkedBlockingDeque<>();

    String accountCreatedEventData = generateAccountCreatedEvent();
    EntityIdVersionAndEventIds entityIdVersionAndEventIds = saveEvent(localAggregateCrud, accountCreatedEventData);

    CdcProcessor<PublishedEvent> mySQLCdcProcessor = createCdcProcessor();
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

  protected abstract CdcProcessor<PublishedEvent> createCdcProcessor();

  protected void onEventSent(PublishedEvent publishedEvent) {

  }
}
