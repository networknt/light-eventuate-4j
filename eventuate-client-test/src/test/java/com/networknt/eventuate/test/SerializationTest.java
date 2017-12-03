package com.networknt.eventuate.test;

import com.networknt.eventuate.common.impl.JSonMapper;
import com.networknt.eventuate.test.domain.AccountCreatedEvent;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;

public class SerializationTest {

  @Test
  public void shouldSerdeAccountCreatedEvent() {

    AccountCreatedEvent event = new AccountCreatedEvent(new BigDecimal("123.45"));
    String json = JSonMapper.toJson(event);
    AccountCreatedEvent event2 = JSonMapper.fromJson(json, AccountCreatedEvent.class);

    assertEquals(event, event2);
  }
}
