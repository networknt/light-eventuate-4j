package com.networknt.eventuate.debezium;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.debezium.config.Configuration;
import io.debezium.embedded.EmbeddedEngine;
import com.networknt.eventuate.cdccore.AggregateTopicMapping;
import com.networknt.eventuate.cdccore.PublishedEvent;
import com.networknt.eventuate.cdccore.kafka.producer.CdcKafkaProducer;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListener;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.storage.KafkaOffsetBackingStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.concurrent.*;

/**
 * Subscribes to changes made to EVENTS table and publishes them to aggregate topics
 */
public abstract class EventTableChangesToAggregateTopicRelay implements TableChangeToTopicRelay{

    protected Logger logger = LoggerFactory.getLogger(getClass());

    protected CdcKafkaProducer producer;
    protected EmbeddedEngine engine;

    public static String kafkaBootstrapServers;
    protected final String dbHost;
    protected final int dbPort;
    protected final String dbUser;
    protected final String dbPassword;
    protected final String dbName;
    protected final LeaderSelector leaderSelector;

    public EventTableChangesToAggregateTopicRelay(String kafkaBootstrapServers,
                                                  String dbHost, int dbPort,
                                                  String dbUser, String dbPassword, String dbName, CuratorFramework client) {
        this.kafkaBootstrapServers = kafkaBootstrapServers;
        this.dbHost = dbHost;
        this.dbPort = dbPort;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;
        this.dbName = dbName;
        leaderSelector = new LeaderSelector(client, "/eventuatelocal/cdc/leader", new LeaderSelectorListener() {

            @Override
            public void takeLeadership(CuratorFramework client) throws Exception {
                takeLeadership();
            }

            private void takeLeadership() throws InterruptedException {
                logger.info("Taking leadership");
                try {
                    CompletableFuture<Object> completion = startCapturingChanges();
                    try {
                        completion.get();
                    } catch (InterruptedException e) {
                        logger.error("Interrupted while taking leadership");
                    }
                } catch (Throwable t) {
                    logger.error("In takeLeadership", t);
                    throw t instanceof RuntimeException ? (RuntimeException)t : new RuntimeException(t);
                } finally {
                    logger.debug("TakeLeadership returning");
                }
            }

            @Override
            public void stateChanged(CuratorFramework client, ConnectionState newState) {

                logger.debug("StateChanged: {}", newState);

                switch (newState) {
                    case SUSPENDED:
                        resignLeadership();
                        break;

                    case RECONNECTED:
                        try {
                            takeLeadership();
                        } catch (InterruptedException e) {
                            logger.error("While handling RECONNECTED", e);
                        }
                        break;

                    case LOST:
                        resignLeadership();
                        break;
                }
            }

            private void resignLeadership() {
                logger.info("Resigning leadership");
                try {
                    stopCapturingChanges();
                } catch (InterruptedException e) {
                    logger.error("While handling SUSPEND", e);
                }
            }
        });
    }

    @Override
    public void start() {
        logger.info("CDC initialized. Ready to become leader");
        leaderSelector.start();
    }

    public abstract CompletableFuture<Object> startCapturingChanges() throws InterruptedException;


    @Override
    public void stop() throws InterruptedException {
        //stopCapturingChanges();
        leaderSelector.close();
    }

    public void stopCapturingChanges() throws InterruptedException {

        logger.debug("Stopping to capture changes");

        if (producer != null)
            producer.close();


        if (engine != null) {

            logger.debug("Stopping Debezium engine");
            engine.stop();

            try {
                while (!engine.await(30, TimeUnit.SECONDS)) {
                    logger.debug("Waiting another 30 seconds for the embedded engine to shut down");
                }
            } catch (InterruptedException e) {
                Thread.interrupted();
            }
        }
    }

    public abstract void handleEvent(SourceRecord sourceRecord) ;


    public static String toJson(PublishedEvent eventInfo) {
        ObjectMapper om = new ObjectMapper();
        try {
            return om.writeValueAsString(eventInfo);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

//  public static class MyKafkaOffsetBackingStore extends KafkaOffsetBackingStore {
//
//    @Override
//    public void configure(WorkerConfig configs) {
//      Map<String, Object> updatedConfig = new HashMap<>(configs.originals());
//      updatedConfig.put("bootstrap.servers", kafkaBootstrapServers);
//      super.configure(new WorkerConfig(configs.));
//    }
//  }

}
