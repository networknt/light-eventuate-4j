package com.networknt.eventuate.cdc.mysql.binlog;

import com.networknt.eventuate.common.impl.JSonMapper;
import com.networknt.eventuate.server.common.BinlogFileOffset;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.util.Map;

public class DebeziumBinlogOffsetKafkaStore extends OffsetKafkaStore {

  public DebeziumBinlogOffsetKafkaStore(String dbHistoryTopicName) {
    super(dbHistoryTopicName);
  }

  @Override
  protected BinlogFileOffset handleRecord(ConsumerRecord<String, String> record) {
    Map<String, Object> map = JSonMapper.fromJson(record.value(), Map.class);
    Object pos = map.get("pos");
    return new BinlogFileOffset((String)map.get("file"), pos instanceof Long ? ((Long) pos) : ((Integer) pos));
  }
}
