package com.networknt.eventuate.server.common;

import com.networknt.eventuate.server.common.exception.EventuateLocalPublishingException;
import com.networknt.eventuate.kafka.producer.EventuateKafkaProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class CdcKafkaPublisher<EVENT> {

  protected PublishingStrategy<EVENT> publishingStrategy;
  protected EventuateKafkaProducer producer;
  private Logger logger = LoggerFactory.getLogger(this.getClass());

  public CdcKafkaPublisher(PublishingStrategy<EVENT> publishingStrategy) {
    this.publishingStrategy = publishingStrategy;
  }

  public void start() {
    logger.debug("Starting CdcKafkaPublisher");
    producer = new EventuateKafkaProducer();
    logger.debug("Starting CdcKafkaPublisher");
  }

  public abstract void handleEvent(EVENT publishedEvent) throws EventuateLocalPublishingException;

  public void stop() {
    logger.debug("Stopping kafka producer");
    if (producer != null)
      producer.close();
  }

}
