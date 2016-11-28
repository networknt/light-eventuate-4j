package com.networknt.eventuate.jdbc;

import com.networknt.eventuate.common.Int128;

public interface IdGenerator {
  Int128 genId();
}
