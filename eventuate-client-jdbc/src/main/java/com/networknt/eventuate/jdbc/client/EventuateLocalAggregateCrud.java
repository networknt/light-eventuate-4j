package com.networknt.eventuate.jdbc.client;


import com.networknt.eventuate.jdbc.common.AbstractJdbcAggregateCrud;
import com.networknt.eventuate.jdbc.common.EventuateJdbcAccess;

/**
 * A JDBC-based aggregate store
 */
public class EventuateLocalAggregateCrud extends AbstractJdbcAggregateCrud {
  public EventuateLocalAggregateCrud(EventuateJdbcAccess eventuateJdbcAccess) {
    super(eventuateJdbcAccess);
  }

}
