package com.networknt.eventuate.cdc.mysql;

import com.networknt.config.Config;
import com.networknt.eventuate.server.common.BinlogFileOffset;
import com.networknt.eventuate.common.impl.JSonMapper;
import com.networknt.eventuate.kafka.KafkaConfig;
import com.networknt.eventuate.kafka.producer.EventuateKafkaProducer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.util.Arrays;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class DatabaseBinlogOffsetKafkaStore extends OffsetKafkaStore {

  private final String mySqlBinaryLogClientName;

  private ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(1);
  private EventuateKafkaProducer eventuateKafkaProducer;

  private final static int N = 20;

  private Optional<BinlogFileOffset> recordToSave = Optional.empty();

  public DatabaseBinlogOffsetKafkaStore(String dbHistoryTopicName, String mySqlBinaryLogClientName, EventuateKafkaProducer eventuateKafkaProducer) {
    super(dbHistoryTopicName);

    this.mySqlBinaryLogClientName = mySqlBinaryLogClientName;
    this.eventuateKafkaProducer = eventuateKafkaProducer;

    scheduledExecutorService.scheduleAtFixedRate(this::scheduledBinlogFilenameAndOffsetUpdate, 5, 5, TimeUnit.SECONDS);
  }

  public synchronized void scheduledBinlogFilenameAndOffsetUpdate() {
    this.recordToSave.ifPresent(this::store);
    this.recordToSave = Optional.empty();
  }

  public synchronized void save(BinlogFileOffset binlogFileOffset) {
    this.recordToSave = Optional.of(binlogFileOffset);
  }

  @Override
  protected BinlogFileOffset handleRecord(ConsumerRecord<String, String> record) {
    if (record.key().equals(mySqlBinaryLogClientName)) {
      return JSonMapper.fromJson(record.value(), BinlogFileOffset.class);
    }
    return null;
  }

  public synchronized void stop() {
    if (this.recordToSave.isPresent())
      this.store(this.recordToSave.get());
    this.scheduledExecutorService.shutdown();
  }

  private synchronized void store(BinlogFileOffset binlogFileOffset) {
    try {
      eventuateKafkaProducer.send(
              dbHistoryTopicName,
              mySqlBinaryLogClientName,
              JSonMapper.toJson(
                      binlogFileOffset
              )
      ).get();
    } catch (RuntimeException e) {
      e.printStackTrace();
      throw e;
    } catch (Throwable e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }
}
