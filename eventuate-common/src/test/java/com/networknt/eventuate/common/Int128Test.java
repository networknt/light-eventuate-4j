package com.networknt.eventuate.common;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class Int128Test {

  @Test
  public void shouldParse() {
    String s = "00000153812efe94-0242ac1100800000";
    Int128 x = Int128.fromString(s);
    assertEquals(s, x.asString());
  }

  @Test
  public void shouldParse2() {
    Int128 x = new Int128(15, 3);
    String s = "000000000000000f-0000000000000003";
    assertEquals(s, x.asString());
  }


  @Test
  public void shouldParse3() {
    String s = "000001568d0065ee-9246580c3b090000";
    Int128 x = Int128.fromString(s);
    assertEquals(s, x.asString());
  }

  @Test
  public void shouldParse4() {
    String s = "900001568d0065ee-9246580c3b090000";
    Int128 x = Int128.fromString(s);
    assertEquals(s, x.asString());
  }
}
