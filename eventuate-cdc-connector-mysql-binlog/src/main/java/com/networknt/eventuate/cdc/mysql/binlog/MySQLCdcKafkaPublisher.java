package com.networknt.eventuate.cdc.mysql.binlog;

import com.networknt.eventuate.server.common.BinLogEvent;
import com.networknt.eventuate.server.common.CdcKafkaPublisher;
import com.networknt.eventuate.server.common.PublishingStrategy;
import com.networknt.eventuate.server.common.exception.EventuateLocalPublishingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class MySQLCdcKafkaPublisher<EVENT extends BinLogEvent> extends CdcKafkaPublisher<EVENT> {

  private DatabaseBinlogOffsetKafkaStore binlogOffsetKafkaStore;
  private Logger logger = LoggerFactory.getLogger(this.getClass());
  private DuplicatePublishingDetector duplicatePublishingDetector;

  public MySQLCdcKafkaPublisher(DatabaseBinlogOffsetKafkaStore binlogOffsetKafkaStore, String kafkaBootstrapServers, PublishingStrategy<EVENT> publishingStrategy) {
    super(publishingStrategy);

    this.binlogOffsetKafkaStore = binlogOffsetKafkaStore;
    this.duplicatePublishingDetector = new DuplicatePublishingDetector();
  }

  @Override
  public void handleEvent(EVENT publishedEvent) throws EventuateLocalPublishingException {
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

}
