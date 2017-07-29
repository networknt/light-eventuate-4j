package com.networknt.eventuate.common.impl.schemametadata;


import com.networknt.eventuate.common.impl.EventIdTypeAndData;

import java.util.List;
import java.util.Optional;

public interface EventSchemaMetadataManager {

  Optional<String> currentVersion(Class clasz);

  List<EventIdTypeAndData> upcastEvents(Class clasz, List<EventIdTypeAndData> events);
}
