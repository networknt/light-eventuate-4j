package com.networknt.eventuate.test.util;

import com.networknt.eventuate.common.Aggregate;
import com.networknt.eventuate.common.Event;
import com.networknt.eventuate.common.EventEntity;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public abstract class AbstractAggregateEventTest<A extends Aggregate<A>, T extends Event> {

  private Class<A> aggregateClass;
  private Class<T> eventClass;

  protected AbstractAggregateEventTest(Class<A> aggregateClass, Class<T> eventClass) {
    this.aggregateClass = aggregateClass;
    this.eventClass = eventClass;
  }

  @Test
  public void entityEventShouldReferenceAggregateClass() {
    assertEquals(aggregateClass.getName(), eventClass.getAnnotation(EventEntity.class).entity());
  }
}
