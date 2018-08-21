package com.networknt.eventuate.solace.consumer;


import com.solacesystems.jcsmp.BytesXMLMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.BiConsumer;

/**
 * Processes a Solace topics
 *
 */
public class SolaceMessageProcessor {
  private Logger logger = LoggerFactory.getLogger(getClass());

  private BiConsumer<BytesXMLMessage, BiConsumer<Void, Throwable>> handler;

  public SolaceMessageProcessor(BiConsumer<BytesXMLMessage, BiConsumer<Void, Throwable>> handler) {
    this.handler = handler;
  }


  public void process(BytesXMLMessage record) {
      handler.accept(record, (result, t) -> {
        if (t != null) {
          logger.error("Got exception: ", t);
        } else {
          logger.debug("Process message {} {}", record.getAckMessageId());
        }
      });
  }

}
