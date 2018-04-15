package com.networknt.eventuate.cdc.mysql.binlog;

import com.networknt.eventuate.server.common.BinLogEvent;
import com.networknt.eventuate.server.common.BinlogFileOffset;
import com.networknt.eventuate.server.common.CdcProcessor;

import java.util.Optional;
import java.util.function.Consumer;

public class MySQLCdcProcessor<EVENT extends BinLogEvent> implements CdcProcessor<EVENT> {

  private MySqlBinaryLogClient<EVENT> mySqlBinaryLogClient;
  private DatabaseBinlogOffsetKafkaStore binlogOffsetKafkaStore;
  private DebeziumBinlogOffsetKafkaStore debeziumBinlogOffsetKafkaStore;

  public MySQLCdcProcessor(MySqlBinaryLogClient<EVENT> mySqlBinaryLogClient,
                           DatabaseBinlogOffsetKafkaStore binlogOffsetKafkaStore,
                           DebeziumBinlogOffsetKafkaStore debeziumBinlogOffsetKafkaStore) {

    this.mySqlBinaryLogClient = mySqlBinaryLogClient;
    this.binlogOffsetKafkaStore = binlogOffsetKafkaStore;
    this.debeziumBinlogOffsetKafkaStore = debeziumBinlogOffsetKafkaStore;
  }

  public void start(Consumer<EVENT> eventConsumer) {
    Optional<BinlogFileOffset> binlogFileOffset = binlogOffsetKafkaStore.getLastBinlogFileOffset();

    if (!binlogFileOffset.isPresent()) {
      binlogFileOffset = debeziumBinlogOffsetKafkaStore.getLastBinlogFileOffset();
    }

    Optional<BinlogFileOffset> startingBinlogFileOffset = binlogFileOffset;

    try {
      mySqlBinaryLogClient.start(startingBinlogFileOffset, new Consumer<EVENT>() {
        private boolean couldReadDuplicateEntries = true;

        @Override
        public void accept(EVENT publishedEvent) {
          if (couldReadDuplicateEntries) {
            if (startingBinlogFileOffset.map(s -> s.isSameOrAfter(publishedEvent.getBinlogFileOffset())).orElse(false)) {
              return;
            } else {
              couldReadDuplicateEntries = false;
            }
          }
          eventConsumer.accept(publishedEvent);
        }
      });
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void stop() {
    mySqlBinaryLogClient.stop();
    binlogOffsetKafkaStore.stop();
  }
}
