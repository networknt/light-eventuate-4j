package com.networknt.eventuate.server.jdbckafkastore;

import com.networknt.eventuate.jdbc.AbstractJdbcAggregateCrud;
import com.networknt.eventuate.jdbc.EventuateJdbcAccess;

/**
 * A JDBC-based aggregate store
 */
public class EventuateLocalAggregateCrud extends AbstractJdbcAggregateCrud {
  public EventuateLocalAggregateCrud(EventuateJdbcAccess eventuateJdbcAccess) {
    super(eventuateJdbcAccess);
  }

}
