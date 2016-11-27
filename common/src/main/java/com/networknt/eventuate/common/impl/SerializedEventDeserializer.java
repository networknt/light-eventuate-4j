package com.networknt.eventuate.common.impl;

import com.networknt.eventuate.common.DispatchedEvent;
import com.networknt.eventuate.common.Event;

import java.util.Optional;

public interface SerializedEventDeserializer {
  Optional<DispatchedEvent<Event>> toDispatchedEvent(SerializedEvent se);
}
