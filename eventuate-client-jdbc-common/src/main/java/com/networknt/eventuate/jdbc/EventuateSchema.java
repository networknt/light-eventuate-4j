package com.networknt.eventuate.jdbc;

import com.networknt.utility.StringUtil;

public class EventuateSchema {
  public static final String DEFAULT_SCHEMA = "eventuate";
  public static final String EMPTY_SCHEMA = "none";

  private final String eventuateDatabaseSchema;

  public EventuateSchema() {
    eventuateDatabaseSchema = DEFAULT_SCHEMA;
  }

  public EventuateSchema(String eventuateDatabaseSchema) {
    this.eventuateDatabaseSchema = StringUtil.isNullOrEmpty(eventuateDatabaseSchema) ? DEFAULT_SCHEMA : eventuateDatabaseSchema;
  }

  public String getEventuateDatabaseSchema() {
    return eventuateDatabaseSchema;
  }

  public boolean isEmpty() {
    return EMPTY_SCHEMA.equals(eventuateDatabaseSchema);
  }

  public boolean isDefault() {
    return DEFAULT_SCHEMA.equals(eventuateDatabaseSchema);
  }

  public String qualifyTable(String table) {
    if (isEmpty()) return table;

    String schema = isDefault() ? DEFAULT_SCHEMA : eventuateDatabaseSchema;

    return String.format("%s.%s", schema, table);
  }
}
