package com.networknt.eventuate.common.impl;

import com.networknt.eventuate.common.Int128;
import org.junit.Test;

import java.math.BigDecimal;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.*;


public class JSonMapperTest {

  static class EmptyEvent {

  }

  static class SomeEvent {
    private Int128 id;
    private BigDecimal amount;
    private String nullProperty;
    private String anotherProperty;

    public SomeEvent() {
    }

    public SomeEvent(Int128 id, BigDecimal amount, String anotherProperty) {
      this.id = id;
      this.amount = amount;
      this.anotherProperty = anotherProperty;
    }

    public Int128 getId() {
      return id;
    }

    public void setId(Int128 id) {
      this.id = id;
    }

    public BigDecimal getAmount() {
      return amount;
    }

    public void setAmount(BigDecimal amount) {
      this.amount = amount;
    }

    public String getNullProperty() {
      return nullProperty;
    }

    public void setNullProperty(String nullProperty) {
      this.nullProperty = nullProperty;
    }

    public String getAnotherProperty() {
      return anotherProperty;
    }

    public void setAnotherProperty(String anotherProperty) {
      this.anotherProperty = anotherProperty;
    }
  }

  static class SubsetEvent {
    private Int128 id;
    private String amount;

    public Int128 getId() {
      return id;
    }

    public void setId(Int128 id) {
      this.id = id;
    }

    public String getAmount() {
      return amount;
    }

    public void setAmount(String amount) {
      this.amount = amount;
    }
  }


  private final String amountAsString = "1345.13";
  private SomeEvent original = new SomeEvent(new Int128(5, 7), new BigDecimal(amountAsString), "foo");


  @Test
  public void shouldSerdeEmpty() {
    String s = JSonMapper.toJson(new EmptyEvent());
    EmptyEvent x = JSonMapper.fromJson(s, EmptyEvent.class);
    assertNotNull(x);
  }

  @Test
  public void shouldSerdeSomeEvent() {
    String s = JSonMapper.toJson(original);
    SomeEvent x = JSonMapper.fromJson(s, SomeEvent.class);

    assertThat(s, not(containsString("nullProperty")));

    //assertTrue(EqualsBuilder.reflectionEquals(original, x));
    assertEquals(original.getId(), x.getId());
    assertEquals(original.getAmount(), x.getAmount());
    assertEquals(original.getAnotherProperty(), x.getAnotherProperty());
    assertEquals(original.getNullProperty(), x.getNullProperty());
  }


  @Test
  public void shouldDeserIgnoringUnknowns() {
    String s = JSonMapper.toJson(original);
    SubsetEvent x = JSonMapper.fromJson(s, SubsetEvent.class);
    assertEquals(original.getId(), x.getId());
    assertEquals(amountAsString, x.getAmount());
  }
}