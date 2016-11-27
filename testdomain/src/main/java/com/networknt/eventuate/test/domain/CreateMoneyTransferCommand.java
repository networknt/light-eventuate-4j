package com.networknt.eventuate.test.domain;

public class CreateMoneyTransferCommand implements MoneyTransferCommand {
  private TransferDetails details;

  public TransferDetails getDetails() {
    return details;
  }

  public CreateMoneyTransferCommand(TransferDetails details) {

    this.details = details;
  }
}
