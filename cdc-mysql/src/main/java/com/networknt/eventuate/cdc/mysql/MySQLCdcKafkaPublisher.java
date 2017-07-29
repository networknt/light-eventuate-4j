package com.networknt.eventuate.cdc.mysql;

import com.networknt.eventuate.cdc.common.BinLogEvent;
import com.networknt.eventuate.cdc.mysql.exception.EventuateLocalPublishingException;
import com.networknt.eventuate.kafka.producer.EventuateKafkaProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class MySQLCdcKafkaPublisher<M extends BinLogEvent> {

  private String kafkaBootstrapServers;
  private PublishingStrategy<M> publishingStrategy;
  private DatabaseBinlogOffsetKafkaStore binlogOffsetKafkaStore;
  private EventuateKafkaProducer producer;
  private Logger logger = LoggerFactory.getLogger(this.getClass());
  private DuplicatePublishingDetector duplicatePublishingDetector;

  public MySQLCdcKafkaPublisher(DatabaseBinlogOffsetKafkaStore binlogOffsetKafkaStore, String kafkaBootstrapServers, PublishingStrategy<M> publishingStrategy) {
    this.binlogOffsetKafkaStore = binlogOffsetKafkaStore;
    this.kafkaBootstrapServers = kafkaBootstrapServers;
    this.publishingStrategy = publishingStrategy;
    this.duplicatePublishingDetector = new DuplicatePublishingDetector(kafkaBootstrapServers);
  }

  public void start() {
    logger.debug("Starting MySQLCdcKafkaPublisher");
    producer = new EventuateKafkaProducer(kafkaBootstrapServers);
    logger.debug("Starting MySQLCdcKafkaPublisher");
  }

  public void handleEvent(M publishedEvent) throws EventuateLocalPublishingException {
    logger.trace("Got record " + publishedEvent.toString());

    String aggregateTopic = publishingStrategy.topicFor(publishedEvent);
    String json = publishingStrategy.toJson(publishedEvent);

    Exception lastException = null;

    for (int i = 0; i < 5; i++) {
      try {
        if (duplicatePublishingDetector.shouldBePublished(publishedEvent.getBinlogFileOffset(), aggregateTopic)) {
          producer.send(
                  aggregateTopic,
                  publishingStrategy.partitionKeyFor(publishedEvent),
                  json
          ).get(10, TimeUnit.SECONDS);

          binlogOffsetKafkaStore.save(publishedEvent.getBinlogFileOffset());
        } else {
          logger.debug("Duplicate event {}", publishedEvent);
        }
        return;
      } catch (Exception e) {
        logger.warn("error publishing to " + aggregateTopic, e);
        lastException = e;
        try {
          Thread.sleep((int) Math.pow(2, i) * 1000);
        } catch (InterruptedException ie) {
          throw new RuntimeException(ie);
        }
      }
    }
    throw new EventuateLocalPublishingException("error publishing to " + aggregateTopic, lastException);
  }


  public void stop() {
    logger.debug("Stopping kafka producer");
    if (producer != null)
      producer.close();
  }

}
