package com.networknt.eventuate.test.domain;


import com.networknt.eventuate.common.Event;
import com.networknt.eventuate.common.EventEntity;

@EventEntity(entity="Account")
public interface AccountEvent extends Event {
}
