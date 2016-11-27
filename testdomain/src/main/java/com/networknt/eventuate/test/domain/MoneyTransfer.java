package com.networknt.eventuate.test.domain;


import com.networknt.eventuate.common.Event;
import com.networknt.eventuate.common.EventUtil;
import com.networknt.eventuate.common.ReflectiveMutableCommandProcessingAggregate;

import java.util.List;

public class MoneyTransfer extends ReflectiveMutableCommandProcessingAggregate<MoneyTransfer, AccountCommand> {

  public List<Event> process(CreateMoneyTransferCommand cmd) {
    return EventUtil.events(new MoneyTransferCreatedEvent(cmd.getDetails()));
  }

  public void apply(AccountCreatedEvent event) {
    // TODO - do something
  }
}
