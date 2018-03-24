package com.networknt.eventuate.cdc.service;

import com.networknt.config.Config;
import com.networknt.eventuate.cdc.mysql.binlog.*;
import com.networknt.eventuate.jdbc.common.EventuateSchema;
import com.networknt.eventuate.server.common.CdcConfig;
import com.networknt.eventuate.server.common.EventTableChangesToAggregateTopicTranslator;
import com.networknt.eventuate.server.common.PublishedEvent;
import com.networknt.eventuate.kafka.KafkaConfig;
import com.networknt.eventuate.kafka.producer.EventuateKafkaProducer;
import com.networknt.eventuate.server.common.PublishedEventPublishingStrategy;
import com.networknt.server.StartupHookProvider;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

/**
 * CdcService StartupHookProvider. start cdc service
 */
public class CdcServiceStartupHookProvider implements StartupHookProvider {

    static String CDC_CONFIG_NAME = "cdc";
    static CdcConfig cdcConfig = (CdcConfig) Config.getInstance().getJsonObjectConfig(CDC_CONFIG_NAME, CdcConfig.class);
    static String KAFKA_CONFIG_NAME = "kafka";
    static KafkaConfig kafkaConfig = (KafkaConfig) Config.getInstance().getJsonObjectConfig(KAFKA_CONFIG_NAME, KafkaConfig.class);

    static HikariDataSource dataSource;

    static {
        dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(cdcConfig.getJdbcUrl());
        dataSource.setUsername(cdcConfig.getDbUser());
        dataSource.setPassword(cdcConfig.getDbPass());
        dataSource.setMaximumPoolSize(cdcConfig.getMaximumPoolSize());
    }

    public static CuratorFramework curatorFramework;
    public static EventTableChangesToAggregateTopicTranslator<PublishedEvent> translator;

    public void onStartup() {
        curatorFramework = makeStartedCuratorClient(cdcConfig.getZookeeper());

        SourceTableNameSupplier supplier = new SourceTableNameSupplier(cdcConfig.getSourceTableName(), "EVENTS");
        IWriteRowsEventDataParser eventDataParser = new WriteRowsEventDataParser(dataSource, supplier.getSourceTableName(), new EventuateSchema());
        MySqlBinaryLogClient<PublishedEvent> mySqlBinaryLogClient = new MySqlBinaryLogClient<>(
                eventDataParser,
                cdcConfig.getDbUser(),
                cdcConfig.getDbPass(),
                cdcConfig.getDbHost(),
                cdcConfig.getDbPort(),
                cdcConfig.getBinlogClientId(),
                supplier.getSourceTableName(),
                cdcConfig.getMySqlBinLogClientName()
        );


        EventuateKafkaProducer eventuateKafkaProducer = new EventuateKafkaProducer();

        DatabaseBinlogOffsetKafkaStore binlogOffsetKafkaStore = new DatabaseBinlogOffsetKafkaStore(
                cdcConfig.getDbHistoryTopicName(), mySqlBinaryLogClient.getName(), eventuateKafkaProducer);

        DebeziumBinlogOffsetKafkaStore debeziumBinlogOffsetKafkaStore = new DebeziumBinlogOffsetKafkaStore(
                cdcConfig.getDbHistoryTopicName());

        MySQLCdcProcessor<PublishedEvent> mySQLCdcProcessor = new MySQLCdcProcessor<>(mySqlBinaryLogClient, binlogOffsetKafkaStore, debeziumBinlogOffsetKafkaStore);

        MySQLCdcKafkaPublisher<PublishedEvent> mySQLCdcKafkaPublisher = new MySQLCdcKafkaPublisher<>(binlogOffsetKafkaStore, kafkaConfig.getBootstrapServers(), new PublishedEventPublishingStrategy());
        translator = new EventTableChangesToAggregateTopicTranslator<>(mySQLCdcKafkaPublisher, mySQLCdcProcessor, curatorFramework, cdcConfig);
        translator.start();

        System.out.println("CdcServiceStartupHookProvider is called");
    }

    CuratorFramework makeStartedCuratorClient(String connectionString) {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        CuratorFramework client = CuratorFrameworkFactory.
                builder().retryPolicy(retryPolicy)
                .connectString(connectionString)
                .build();
        client.start();
        return client;
    }


}
