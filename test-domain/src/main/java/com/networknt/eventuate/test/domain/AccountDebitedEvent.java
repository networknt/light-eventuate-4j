package com.networknt.eventuate.test.domain;

import java.math.BigDecimal;

public class AccountDebitedEvent implements AccountEvent {
  private BigDecimal amount;
  private String transactionId;

  private AccountDebitedEvent() {
  }

  public AccountDebitedEvent(BigDecimal amount, String transactionId) {
    this.amount = amount;
    this.transactionId = transactionId;
  }

  @Override
  public String toString() {
    return "AccountDebitedEvent{" +
            "amount=" + amount +
            ", transactionId='" + transactionId + '\'' +
            '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    AccountDebitedEvent that = (AccountDebitedEvent) o;

    if (amount != null ? !amount.equals(that.amount) : that.amount != null) return false;
    return transactionId != null ? transactionId.equals(that.transactionId) : that.transactionId == null;

  }

  @Override
  public int hashCode() {
    int result = amount != null ? amount.hashCode() : 0;
    result = 31 * result + (transactionId != null ? transactionId.hashCode() : 0);
    return result;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public String getTransactionId() {
    return transactionId;
  }
}
