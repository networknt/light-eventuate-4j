package com.networknt.eventuate.server.common;

public interface BinLogEvent {
  BinlogFileOffset getBinlogFileOffset();
}
