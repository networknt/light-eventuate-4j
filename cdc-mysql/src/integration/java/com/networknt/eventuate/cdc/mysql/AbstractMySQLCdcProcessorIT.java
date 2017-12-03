package com.networknt.eventuate.cdc.mysql;

import com.networknt.eventuate.server.common.CdcProcessor;
import com.networknt.eventuate.server.common.PublishedEvent;
import com.networknt.eventuate.server.test.util.CdcProcessorTest;
import com.networknt.service.SingletonServiceFactory;

public abstract class AbstractMySQLCdcProcessorIT extends CdcProcessorTest {

  private MySqlBinaryLogClient<PublishedEvent> mySqlBinaryLogClient = SingletonServiceFactory.getBean(MySqlBinaryLogClient.class);

  private DatabaseBinlogOffsetKafkaStore binlogOffsetKafkaStore = SingletonServiceFactory.getBean(DatabaseBinlogOffsetKafkaStore.class);

  private DebeziumBinlogOffsetKafkaStore debeziumBinlogOffsetKafkaStore = SingletonServiceFactory.getBean(DebeziumBinlogOffsetKafkaStore.class);

  @Override
  protected CdcProcessor<PublishedEvent> createCdcProcessor() {
    return new MySQLCdcProcessor<>(mySqlBinaryLogClient, binlogOffsetKafkaStore, debeziumBinlogOffsetKafkaStore);
  }

  @Override
  protected void onEventSent(PublishedEvent publishedEvent) {
    binlogOffsetKafkaStore.save(publishedEvent.getBinlogFileOffset());
  }
}
