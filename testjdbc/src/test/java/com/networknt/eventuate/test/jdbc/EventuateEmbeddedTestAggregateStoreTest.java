package com.networknt.eventuate.test.jdbc;

import com.networknt.eventuate.common.*;
import com.networknt.eventuate.common.impl.AggregateCrud;
import com.networknt.eventuate.common.impl.EntityIdVersionAndEventIds;
import com.networknt.eventuate.common.impl.EventTypeAndData;
import com.networknt.eventuate.common.impl.LoadedEvents;
import com.networknt.service.SingletonServiceFactory;
import org.h2.tools.RunScript;
import org.junit.Test;

import javax.sql.DataSource;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static com.networknt.eventuate.test.util.AsyncUtil.await;
import static org.junit.Assert.*;

public class EventuateEmbeddedTestAggregateStoreTest {

  private final EventContext ectx = new EventContext("MyEventToken");
  private final String aggregateType = "MyAggregateType";
  public static DataSource ds;
  static {
    ds = (DataSource)SingletonServiceFactory.getBean(DataSource.class);
    try (Connection connection = ds.getConnection()) {
      // Runscript doesn't work need to execute batch here.
      String schemaResourceName = "/embedded-event-store-schema.sql";
      InputStream in = EventuateEmbeddedTestAggregateStoreTest.class.getResourceAsStream(schemaResourceName);

      if (in == null) {
        throw new RuntimeException("Failed to load resource: " + schemaResourceName);
      }
      InputStreamReader reader = new InputStreamReader(in);
      RunScript.execute(connection, reader);

    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  private AggregateCrud eventStore = (AggregateCrud)SingletonServiceFactory.getBean(AggregateCrud.class);

  @Test
  public void findShouldCompleteWithDuplicateTriggeringEventException() throws ExecutionException, InterruptedException {
    EntityIdVersionAndEventIds eidv = await(eventStore.save(aggregateType,
            Collections.singletonList(new EventTypeAndData("MyEventType", "{}")),
            Optional.of(new SaveOptions().withEventContext(ectx))));
    CompletableFuture<LoadedEvents> c = eventStore.find(aggregateType, eidv.getEntityId(), Optional.of(new FindOptions().withTriggeringEvent(ectx)));
    shouldCompletedExceptionally(c, DuplicateTriggeringEventException.class);
  }

  @Test
  public void updateShouldCompleteWithOptimisticLockingException() throws ExecutionException, InterruptedException {
    EntityIdVersionAndEventIds eidv = await(eventStore.save(aggregateType,
            Collections.singletonList(new EventTypeAndData("MyEventType", "{}")),
            Optional.of(new SaveOptions().withEventContext(ectx))));
    CompletableFuture<EntityIdVersionAndEventIds> c = eventStore.update(new EntityIdAndType(eidv.getEntityId(), aggregateType),
            new Int128(0,0), Collections.singletonList(new EventTypeAndData("MyEventType", "{}")), Optional.of(new UpdateOptions()));
    shouldCompletedExceptionally(c, OptimisticLockingException.class);
  }


  private void shouldCompletedExceptionally(CompletableFuture<?> c, Class<?> exceptionClass) throws InterruptedException, ExecutionException {
    try {
      c.get();
      fail();
    } catch (ExecutionException e) {
      if (!exceptionClass.isInstance(e.getCause()))
        throw e;
    }
  }

}