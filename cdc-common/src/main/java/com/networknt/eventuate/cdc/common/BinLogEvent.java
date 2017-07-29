package com.networknt.eventuate.cdc.common;

public interface BinLogEvent {
  BinlogFileOffset getBinlogFileOffset();
}
