package com.networknt.eventuate.jdbc.client;

import com.networknt.eventuate.common.EventContext;
import com.networknt.eventuate.jdbc.common.EventAndTrigger;
import com.networknt.eventuate.jdbc.common.EventuateJdbcAccessImpl;
import com.networknt.eventuate.jdbc.common.EventuateSchema;
import com.networknt.eventuate.jdbc.common.LoadedSnapshot;

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
