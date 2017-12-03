package com.networknt.eventuate.jdbc;

import org.junit.Before;

import java.util.List;

public class EmptyEventuateJdbcAccessImplTest extends EventuateJdbcAccessImplTest {

  @Override
  protected String readAllEventsSql() {
    return "select * from events";
  }

  @Override
  protected String readAllEntitiesSql() {
    return "select * from entities";
  }

  @Override
  protected String readAllSnapshots() {
    return "select * from snapshots";
  }

  @Before
  public void init() throws Exception {
    List<String> lines = loadSqlScriptAsListOfLines("/eventuate-embedded-schema.sql");
    lines = lines.subList(2, lines.size());
    executeSql(lines);
  }
}
