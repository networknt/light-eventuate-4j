package com.networknt.eventuate.solace.producer;

import com.networknt.config.Config;
import com.networknt.eventuate.solace.SolaceConfig;
import com.solacesystems.jcsmp.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A Solace Producer that send messages to defined Solace instance
 *
 */
public class EventuateSolaceProducer {

  private JCSMPSession session;
  private final JCSMPProperties properties;;

  static final SolaceConfig config = (SolaceConfig) Config.getInstance().getJsonObjectConfig(SolaceConfig.CONFIG_NAME, SolaceConfig.class);
  private static Logger logger = LoggerFactory.getLogger(EventuateSolaceProducer.class);

  public EventuateSolaceProducer() throws JCSMPException {
    properties = new JCSMPProperties();
    properties.setProperty(JCSMPProperties.HOST, config.getHost());     // host:port
    properties.setProperty(JCSMPProperties.USERNAME, config.getUsername()); // client-username
    properties.setProperty(JCSMPProperties.PASSWORD, config.getPassword()); // client-password
    properties.setProperty(JCSMPProperties.VPN_NAME,  config.getVpn()); // message-vpn
    session = JCSMPFactory.onlyInstance().createSession(properties);
  }


  public void setSession (JCSMPSession session) {
    this.session = session;
  }

  /**
   * Update the aggregate
   * @param topic Solace topic
   * @param body message body
   */
  public void send(String topic, String body) throws JCSMPException {

    session.connect();
    final Topic topicEntity = JCSMPFactory.onlyInstance().createTopic(topic);

    /** Anonymous inner-class for handling publishing events */
    XMLMessageProducer prod = session.getMessageProducer(new JCSMPStreamingPublishEventHandler() {
      @Override
      public void responseReceived(String messageID) {
        System.out.println("Producer received response for msg: " + messageID);
      }
      @Override
      public void handleError(String messageID, JCSMPException e, long timestamp) {
        System.out.printf("Producer received error for msg: %s@%s - %s%n",
                messageID,timestamp,e);
      }
    });
    // Publish-only session is now hooked up and running!

    TextMessage msg = JCSMPFactory.onlyInstance().createMessage(TextMessage.class);

    msg.setText(body);
    prod.send(msg,topicEntity);
    prod.close();

  }

  public void close() {
    session.closeSession();
  }
}
