package com.networknt.eventuate.cdc.mysql.binlog;

import com.networknt.config.Config;
import com.networknt.eventuate.jdbc.EventuateSchema;
import com.networknt.eventuate.kafka.KafkaConfig;
import com.networknt.eventuate.kafka.producer.EventuateKafkaProducer;
import com.networknt.eventuate.server.common.*;
import com.networknt.service.SingletonServiceFactory;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class MySqlCdcServiceInitializer {

    public static String CDC_CONFIG_NAME = "cdc";
    public static CdcConfig cdcConfig = (CdcConfig) Config.getInstance().getJsonObjectConfig(CDC_CONFIG_NAME, CdcConfig.class);
    public static String KAFKA_CONFIG_NAME = "kafka";
    public static KafkaConfig kafkaConfig = (KafkaConfig) Config.getInstance().getJsonObjectConfig(KAFKA_CONFIG_NAME, KafkaConfig.class);

    public EventuateSchema eventuateSchema() {
        return new EventuateSchema();
    }

    public SourceTableNameSupplier sourceTableNameSupplier() {
        return new SourceTableNameSupplier(cdcConfig.getSourceTableName(), "EVENTS");
    }

    public IWriteRowsEventDataParser<PublishedEvent> eventDataParser() {
        DataSource dataSource = SingletonServiceFactory.getBean(DataSource.class);
        EventuateSchema eventuateSchema = SingletonServiceFactory.getBean(EventuateSchema.class);
        SourceTableNameSupplier sourceTableNameSupplier = SingletonServiceFactory.getBean(SourceTableNameSupplier.class);
        return new WriteRowsEventDataParser(dataSource, sourceTableNameSupplier.getSourceTableName(), eventuateSchema);
    }


    public MySqlBinaryLogClient<PublishedEvent> mySqlBinaryLogClient() throws IOException, TimeoutException {
        IWriteRowsEventDataParser<PublishedEvent> eventDataParser = SingletonServiceFactory.getBean(IWriteRowsEventDataParser.class);
        SourceTableNameSupplier sourceTableNameSupplier = SingletonServiceFactory.getBean(SourceTableNameSupplier.class);
        return new MySqlBinaryLogClient<>(eventDataParser,
                cdcConfig.getDbUser(),
                cdcConfig.getDbPass(),
                cdcConfig.getDbHost(),
                cdcConfig.getDbPort(),
                cdcConfig.getBinlogClientId(),
                sourceTableNameSupplier.getSourceTableName(),
                cdcConfig.getMySqlBinLogClientName());
    }

    public EventuateKafkaProducer eventuateKafkaProducer() {
        return new EventuateKafkaProducer();
    }

    public DatabaseBinlogOffsetKafkaStore binlogOffsetKafkaStore() {
        MySqlBinaryLogClient<PublishedEvent> mySqlBinaryLogClient = SingletonServiceFactory.getBean(MySqlBinaryLogClient.class);
        EventuateKafkaProducer eventuateKafkaProducer = SingletonServiceFactory.getBean(EventuateKafkaProducer.class);
        return new DatabaseBinlogOffsetKafkaStore(
                cdcConfig.getDbHistoryTopicName(),
                mySqlBinaryLogClient.getName(),
                eventuateKafkaProducer);
    }

    public DebeziumBinlogOffsetKafkaStore debeziumBinlogOffsetKafkaStore() {
        return new DebeziumBinlogOffsetKafkaStore(cdcConfig.getOldDbHistoryTopicName());
    }


    public CdcProcessor<PublishedEvent> mySQLCdcProcessor() {
        MySqlBinaryLogClient<PublishedEvent> mySqlBinaryLogClient = SingletonServiceFactory.getBean(MySqlBinaryLogClient.class);
        DatabaseBinlogOffsetKafkaStore binlogOffsetKafkaStore = SingletonServiceFactory.getBean(DatabaseBinlogOffsetKafkaStore.class);
        DebeziumBinlogOffsetKafkaStore debeziumBinlogOffsetKafkaStore = SingletonServiceFactory.getBean(DebeziumBinlogOffsetKafkaStore.class);
        return new MySQLCdcProcessor<>(mySqlBinaryLogClient, binlogOffsetKafkaStore, debeziumBinlogOffsetKafkaStore);
    }

    public CdcKafkaPublisher<PublishedEvent> mySQLCdcKafkaPublisher() {
        DatabaseBinlogOffsetKafkaStore binlogOffsetKafkaStore = SingletonServiceFactory.getBean(DatabaseBinlogOffsetKafkaStore.class);
        PublishingStrategy<PublishedEvent> publishingStrategy = SingletonServiceFactory.getBean(PublishingStrategy.class);
        return new MySQLCdcKafkaPublisher<>(binlogOffsetKafkaStore, kafkaConfig.getBootstrapServers(), publishingStrategy);
    }

    public CuratorFramework curatorFramework() {
        String connectionString = cdcConfig.getZookeeper();
        return makeStartedCuratorClient(connectionString);
    }

    public EventTableChangesToAggregateTopicTranslator<PublishedEvent> mySqlEventTableChangesToAggregateTopicTranslator() {
        CdcKafkaPublisher<PublishedEvent> mySQLCdcKafkaPublisher = SingletonServiceFactory.getBean(CdcKafkaPublisher.class);
        CdcProcessor<PublishedEvent> mySQLCdcProcessor = SingletonServiceFactory.getBean(CdcProcessor.class);
        CuratorFramework curatorFramework = SingletonServiceFactory.getBean(CuratorFramework.class);

        return new EventTableChangesToAggregateTopicTranslator<>(mySQLCdcKafkaPublisher,
                mySQLCdcProcessor,
                curatorFramework,
                cdcConfig);
    }

    static CuratorFramework makeStartedCuratorClient(String connectionString) {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        CuratorFramework client = CuratorFrameworkFactory.
                builder().retryPolicy(retryPolicy)
                .connectString(connectionString)
                .build();
        client.start();
        return client;
    }
}
