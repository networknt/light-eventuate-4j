package com.networknt.eventuate.server.jdbckafkastore;

import com.networknt.eventuate.common.EventContext;
import com.networknt.eventuate.jdbc.EventAndTrigger;
import com.networknt.eventuate.jdbc.EventuateJdbcAccessImpl;
import com.networknt.eventuate.jdbc.EventuateSchema;
import com.networknt.eventuate.jdbc.LoadedSnapshot;

import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;

public class EventuateLocalJdbcAccess extends EventuateJdbcAccessImpl {

  public EventuateLocalJdbcAccess(DataSource dataSource) {
    super(dataSource);
  }

  public EventuateLocalJdbcAccess(DataSource dataSource, EventuateSchema eventuateSchema) {
    super(dataSource, eventuateSchema);
  }

  @Override
  protected void checkSnapshotForDuplicateEvent(LoadedSnapshot ss, EventContext te) {
    SnapshotTriggeringEvents.checkSnapshotForDuplicateEvent(ss, te);
  }

  @Override
  protected String snapshotTriggeringEvents(Optional<LoadedSnapshot> previousSnapshot, List<EventAndTrigger> events, Optional<EventContext> eventContext) {
    return SnapshotTriggeringEvents.snapshotTriggeringEvents(previousSnapshot, events, eventContext);
  }

}
