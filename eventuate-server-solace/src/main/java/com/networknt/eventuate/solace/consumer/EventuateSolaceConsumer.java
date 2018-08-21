package com.networknt.eventuate.solace.consumer;

import com.networknt.config.Config;
import com.networknt.eventuate.solace.SolaceConfig;
import com.solacesystems.jcsmp.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

/**
 * A Solace consumer that  supports asynchronous message processing
 */
public class EventuateSolaceConsumer {

  static final SolaceConfig config = (SolaceConfig) Config.getInstance().getJsonObjectConfig(SolaceConfig.CONFIG_NAME, SolaceConfig.class);
  private static Logger logger = LoggerFactory.getLogger(EventuateSolaceConsumer.class);


  private final BiConsumer<BytesXMLMessage, BiConsumer<Void, Throwable>> handler;
  private final List<String> topics;
  private JCSMPSession session;
  private XMLMessageConsumer cons;
  private JCSMPProperties consumerProperties;

  /**
   *
   * @param handler defined message handler
   * @param topics topics Kafka topic list
   */
  public EventuateSolaceConsumer(BiConsumer<BytesXMLMessage, BiConsumer<Void, Throwable>> handler, List<String> topics) {

    this.handler = handler;
    this.topics = topics;
    this.consumerProperties = new JCSMPProperties();
    this.consumerProperties.setProperty(JCSMPProperties.HOST, config.getHost());     // host:port
    this.consumerProperties.setProperty(JCSMPProperties.USERNAME, config.getUsername()); // client-username
    this.consumerProperties.setProperty(JCSMPProperties.PASSWORD, config.getPassword()); // client-password
    this.consumerProperties.setProperty(JCSMPProperties.VPN_NAME,  config.getVpn()); // message-vpn
  }


  /**
   * start Solace consumer process
   */
  public void start() {
    try {

      this.session = JCSMPFactory.onlyInstance().createSession(consumerProperties);
      SolaceMessageProcessor processor = new SolaceMessageProcessor(handler);
      session.connect();

      final CountDownLatch latch = new CountDownLatch(1); // used for
      // synchronizing b/w threads
      /** Anonymous inner-class for MessageListener
       *  This demonstrates the async threaded message callback */
      this.cons = session.getMessageConsumer(new XMLMessageListener() {
        @Override
        public void onReceive(BytesXMLMessage msg) {
          if (msg instanceof TextMessage) {
            processor.process(msg);
          } else {
            logger.info("Message received. Format not support");
          }
          logger.info("Message Dump:%n%s%n",msg.dump());
          latch.countDown();  // unblock main thread
        }

        @Override
        public void onException(JCSMPException e) {
          logger.error("Consumer received exception: %s%n",e);
          latch.countDown();  // unblock main thread
        }
      });
      for (String topic : topics) {
        Topic topicEntity = JCSMPFactory.onlyInstance().createTopic(topic);
        session.addSubscription(topicEntity);
      }
      System.out.println("Connected. Awaiting message...");
      cons.start();
      logger.debug("Subscribed to {}",  topics);
      try {
        latch.await(); // block here until message received, and latch will flip
      } catch (InterruptedException e) {
        System.out.println("I was awoken while waiting");
      }
    } catch (Exception e) {
      logger.error("Error subscribing", e);
      throw new RuntimeException(e);
    }
  }

  public void stop() {
    cons.close();
    System.out.println("Exiting.");
    session.closeSession();
  }

}
