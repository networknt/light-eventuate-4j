package com.networknt.eventuate.jdbc;

import com.networknt.eventuate.common.EntityIdAndType;
import com.networknt.eventuate.common.impl.AggregateCrudUpdateOptions;
import com.networknt.eventuate.common.impl.EventTypeAndData;
import com.networknt.eventuate.common.impl.LoadedEvents;
import com.networknt.eventuate.common.impl.SerializedSnapshot;
import com.networknt.service.SingletonServiceFactory;
import org.h2.tools.RunScript;
import org.junit.Assert;
import org.junit.Test;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class EventuateJdbcAccessImplTest {

  public static DataSource ds;

  static {
    ds = (DataSource) SingletonServiceFactory.getBean(DataSource.class);
    try (Connection connection = ds.getConnection()) {
      // Runscript doesn't work need to execute batch here.
      String schemaResourceName = "/eventuate-embedded-schema.sql";
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

  private static final String testAggregate = "testAggregate1";
  private static final String testEventType = "testEventType1";
  private static final String testEventData = "testEventData1";


  private EventuateJdbcAccess eventuateJdbcAccess = SingletonServiceFactory.getBean(EventuateJdbcAccess.class);


  protected abstract String readAllEventsSql();

  protected abstract String readAllEntitiesSql();

  protected abstract String readAllSnapshots();

  @Test
  public void testSave() {
    EventTypeAndData eventTypeAndData = new EventTypeAndData(testEventType, testEventData, Optional.empty());

    eventuateJdbcAccess.save(testAggregate, Collections.singletonList(eventTypeAndData), Optional.empty());

  //  Assert.assertEquals(1, dbRowCount(readAllEventsSql()));

//    Assert.assertEquals(1, dbRowCount(readAllEntitiesSql()));
  }

  @Test
  public void testFind() {
    EventTypeAndData eventTypeAndData = new EventTypeAndData(testEventType, testEventData, Optional.empty());

    SaveUpdateResult saveUpdateResult = eventuateJdbcAccess.save(testAggregate, Collections.singletonList(eventTypeAndData), Optional.empty());

    LoadedEvents loadedEvents = eventuateJdbcAccess.find(testAggregate, saveUpdateResult.getEntityIdVersionAndEventIds().getEntityId(), Optional.empty());

    Assert.assertEquals(1, loadedEvents.getEvents().size());
  }

  @Test
  public void testUpdate() {
    EventTypeAndData eventTypeAndData = new EventTypeAndData(testEventType, testEventData, Optional.empty());
    SaveUpdateResult saveUpdateResult = eventuateJdbcAccess.save(testAggregate, Collections.singletonList(eventTypeAndData), Optional.empty());


    EntityIdAndType entityIdAndType = new EntityIdAndType(saveUpdateResult.getEntityIdVersionAndEventIds().getEntityId(), testAggregate);
    eventTypeAndData = new EventTypeAndData("testEventType2", "testEventData2", Optional.empty());

    eventuateJdbcAccess.update(entityIdAndType,
            saveUpdateResult.getEntityIdVersionAndEventIds().getEntityVersion(),
            Collections.singletonList(eventTypeAndData), Optional.of(new AggregateCrudUpdateOptions(Optional.empty(), Optional.of(new SerializedSnapshot("", "")))));

   // Assert.assertEquals(2, dbRowCount(readAllEventsSql()));

//    Assert.assertEquals(1, dbRowCount(readAllEntitiesSql()));

  //  Assert.assertEquals(1, dbRowCount(readAllSnapshots()));

    LoadedEvents loadedEvents = eventuateJdbcAccess.find(testAggregate, saveUpdateResult.getEntityIdVersionAndEventIds().getEntityId(), Optional.empty());
    Assert.assertTrue(loadedEvents.getSnapshot().isPresent());
  }

  protected List<String> loadSqlScriptAsListOfLines(String script) throws IOException {
    try(BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/eventuate-embedded-schema.sql")))) {
      return bufferedReader.lines().collect(Collectors.toList());
    }
  }

  protected void executeSql(List<String> sqlList) {
    dbExecute(sqlList.stream().collect(Collectors.joining("\n")));
  }

  public int dbRowCount(String query) {
    int count = 0;
    try (final Connection connection = ds.getConnection()){
      try (PreparedStatement stmt = connection.prepareStatement(query)) {
        try (ResultSet rs = stmt.executeQuery()) {
          while (rs.next()) {
            count++;
          }
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return count;
  }

  public void dbExecute(String update) {
    try (final Connection connection = ds.getConnection()) {
      try (PreparedStatement ps = connection.prepareStatement(update)) {
        ps.executeUpdate();
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }
}
