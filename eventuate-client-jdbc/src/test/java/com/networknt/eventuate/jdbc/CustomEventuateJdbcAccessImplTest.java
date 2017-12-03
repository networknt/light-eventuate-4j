package com.networknt.eventuate.jdbc;

import com.networknt.service.SingletonServiceFactory;
import org.junit.Before;
import java.util.List;

public class CustomEventuateJdbcAccessImplTest extends EventuateJdbcAccessImplTest {

  static {
    SingletonServiceFactory.setBean(EventuateJdbcAccess.class.getName(), new EventuateJdbcAccessImpl(EventuateJdbcAccessImplTest.ds, new EventuateSchema("custom")));
  }

  @Override
  protected String readAllEventsSql() {
    return "select * from custom.events";
  }

  @Override
  protected String readAllEntitiesSql() {
    return "select * from custom.entities";
  }

  @Override
  protected String readAllSnapshots() {
    return "select * from custom.snapshots";
  }

  @Before
  public void init() throws Exception {
    List<String> lines = loadSqlScriptAsListOfLines("eventuate-embedded-schema.sql");
    for (int i = 0; i < 2; i++) lines.set(i, lines.get(i).replace("eventuate", "custom"));
    executeSql(lines);
  }
}
