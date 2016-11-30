package com.networknt.eventuate.test.util;

import com.networknt.eventuate.common.DispatchedEvent;
import com.networknt.eventuate.common.Event;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;


public class EventTracker {

  private BlockingQueue<DispatchedEvent<Event>> events = new LinkedBlockingQueue<>();

  public BlockingQueue<DispatchedEvent<Event>> getEvents() {
    return events;
  }

  public <T extends Event> void add(DispatchedEvent<T> ctx) {
    events.add((DispatchedEvent<Event>) ctx);
  }

  public <T extends Event> DispatchedEvent<T> assertMessagePublished(String entityId, Class<T> eventClass) {
    return Eventually.eventuallyReturning(30, 1, TimeUnit.SECONDS, () -> {
      DispatchedEvent<Event>[] currentEvents = events.toArray(new DispatchedEvent[events.size()]);
      for (DispatchedEvent<Event> event : currentEvents) {
        if (event.getEntityId().equals(entityId) && event.getEventType().equals(eventClass))
          return (DispatchedEvent<T>)event;
      }
      throw new RuntimeException(String.format("Haven't found event from %s of type %s", entityId, eventClass));
    });
  }
}
