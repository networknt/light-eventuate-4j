package com.networknt.eventuate.test.domain;

import com.networknt.eventuate.common.Event;
import com.networknt.eventuate.common.EventEntity;

@EventEntity(entity="MoneyTransfer")
public interface MoneyTransferEvent extends Event {
}
