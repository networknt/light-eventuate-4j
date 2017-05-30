package com.networknt.eventuate.debezium;


import com.networknt.eventuate.cdccore.AggregateTopicMapping;
import com.networknt.eventuate.cdccore.PublishedEvent;
import com.networknt.eventuate.cdccore.kafka.producer.CdcKafkaProducer;
import io.debezium.config.Configuration;
import io.debezium.embedded.EmbeddedEngine;
import org.apache.curator.framework.CuratorFramework;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.storage.KafkaOffsetBackingStore;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Subscribes to changes made to EVENTS table and publishes them to aggregate topics
 */
public class EventTableChangesToAggregateTopicRelayMysql extends EventTableChangesToAggregateTopicRelay{

    public EventTableChangesToAggregateTopicRelayMysql(String kafkaBootstrapServers,
                                                  String dbHost, int dbPort,
                                                  String dbUser, String dbPassword, String dbName, CuratorFramework client) {
        super(kafkaBootstrapServers, dbHost, dbPort, dbUser, dbPassword, dbName, client);
    }

    @Override
    public CompletableFuture<Object> startCapturingChanges() throws InterruptedException {

        logger.debug("Starting to capture changes");
        producer = new CdcKafkaProducer(kafkaBootstrapServers);

        String connectorName = "my-sql-connector";
        Configuration config = Configuration.create()
                                    /* begin engine properties */
                .with("connector.class",
                        "io.debezium.connector.mysql.MySqlConnector")

                .with("offset.storage", KafkaOffsetBackingStore.class.getName())
                .with("bootstrap.servers", kafkaBootstrapServers)
                .with("offset.storage.topic", "eventuate.local.cdc." + connectorName + ".offset.storage")

                .with("poll.interval.ms", 50)
                .with("offset.flush.interval.ms", 6000)
                                    /* begin connector properties */
                .with("name", connectorName)
                .with("database.hostname", dbHost)
                .with("database.port", dbPort)
                .with("database.user", dbUser)
                .with("database.password", dbPassword)
                .with("database.server.id", 85744)
                .with("database.server.name", "light-event-sourcing")
                //.with("database.whitelist", "dbName")
                .with("database.history",
                        io.debezium.relational.history.KafkaDatabaseHistory.class.getName())
                .with("database.history.kafka.topic",
                        "eventuate.local.cdc." + connectorName + ".history.kafka.topic")
                .with("database.history.kafka.bootstrap.servers",
                        kafkaBootstrapServers)
                .build();

        CompletableFuture<Object> completion = new CompletableFuture<>();
        engine = EmbeddedEngine.create()
                .using((success, message, throwable) -> {
                    if (success)
                        completion.complete(null);
                    else
                        completion.completeExceptionally(new RuntimeException("Engine failed to start" + message, throwable));
                })
                .using(config)
                .notifying(this::handleEvent)
                .build();

        Executor executor = Executors.newCachedThreadPool();
        executor.execute(() -> {
            try {
                engine.run();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        });

        logger.debug("Started engine");
        return completion;
    }

    @Override
    public void handleEvent(SourceRecord sourceRecord) {
        logger.trace("Got record");
        String topic = sourceRecord.topic();
        System.out.println("topic---->:"  + topic);
        if ("light-event-sourcing.eventuate.events".equals(topic)) {
            Struct value = (Struct) sourceRecord.value();
            Struct after = value.getStruct("after");

            String eventId = after.getString("event_id");
            String eventType = after.getString("event_type");
            String eventData = after.getString("event_data");
            String entityType = after.getString("entity_type");
            String entityId = after.getString("entity_id");
            String triggeringEvent = after.getString("triggering_event");
            PublishedEvent pe = new PublishedEvent(eventId,
                    entityId, entityType,
                    eventData,
                    eventType);


            String aggregateTopic = AggregateTopicMapping.aggregateTypeToTopic(entityType);
            String json = toJson(pe);

            if (logger.isInfoEnabled())
                logger.debug("Publishing triggeringEvent={}, event={}", triggeringEvent, json);

            try {
                producer.send(
                        aggregateTopic,
                        entityId,
                        json
                ).get(10, TimeUnit.SECONDS);
            } catch (RuntimeException e) {
                logger.error("error publishing to " + aggregateTopic, e);
                throw e;
            } catch (Throwable e) {
                logger.error("error publishing to " + aggregateTopic, e);
                throw new RuntimeException(e);
            }
        }
    }


}
