package com.networknt.eventuate.test.domain;


import java.math.BigDecimal;

public class AccountCreatedEvent implements AccountEvent {

  private BigDecimal initialBalance;

  private AccountCreatedEvent() {
  }

  public AccountCreatedEvent(BigDecimal initialBalance) {
    this.initialBalance = initialBalance;
  }

  public BigDecimal getInitialBalance() {
    return initialBalance;
  }

  public void setInitialBalance(BigDecimal initialBalance) {
    this.initialBalance = initialBalance;
  }

  @Override
  public String toString() {
    return "AccountCreatedEvent{" +
            "initialBalance=" + initialBalance +
            '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    AccountCreatedEvent that = (AccountCreatedEvent) o;

    return initialBalance != null ? initialBalance.equals(that.initialBalance) : that.initialBalance == null;

  }

  @Override
  public int hashCode() {
    return initialBalance != null ? initialBalance.hashCode() : 0;
  }
}
