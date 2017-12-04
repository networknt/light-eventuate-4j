package com.networknt.eventuate.cdc.mysql.binlog;

import com.networknt.config.Config;
import com.networknt.eventuate.jdbc.EventuateSchema;
import com.networknt.eventuate.kafka.producer.EventuateKafkaProducer;
import com.networknt.eventuate.server.common.*;
import com.networknt.service.SingletonServiceFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class MySqlCdcServiceInitializer {

    private static final String CONFIG_NAME = "cdc";
    private static final CdcConfig config = (CdcConfig) Config.getInstance().getJsonObjectConfig(CONFIG_NAME, CdcConfig.class);

    public EventuateSchema eventuateSchema() {
        return new EventuateSchema();
    }

    public SourceTableNameSupplier sourceTableNameSupplier() {
        return new SourceTableNameSupplier(config.getSourceTableName(), "EVENTS");
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
                config.getDbUser(),
                config.getDbPass(),
                config.getDbHost(),
                config.getDbPort(),
                config.getBinlogClientId(),
                sourceTableNameSupplier.getSourceTableName(),
                config.getMySqlBinLogClientName());
    }

    public EventuateKafkaProducer eventuateKafkaProducer() {
        return new EventuateKafkaProducer();
    }

    public DatabaseBinlogOffsetKafkaStore binlogOffsetKafkaStore() {
        MySqlBinaryLogClient<PublishedEvent> mySqlBinaryLogClient = SingletonServiceFactory.getBean(MySqlBinaryLogClient.class);
        EventuateKafkaProducer eventuateKafkaProducer = SingletonServiceFactory.getBean(EventuateKafkaProducer.class);
        return new DatabaseBinlogOffsetKafkaStore(
                config.getDbHistoryTopicName(),
                mySqlBinaryLogClient.getName(),
                eventuateKafkaProducer);
    }

    public DebeziumBinlogOffsetKafkaStore debeziumBinlogOffsetKafkaStore() {
        return new DebeziumBinlogOffsetKafkaStore(config.getOldDbHistoryTopicName());
    }


    public CdcProcessor<PublishedEvent> mySQLCdcProcessor() {
        MySqlBinaryLogClient<PublishedEvent> mySqlBinaryLogClient = SingletonServiceFactory.getBean(MySqlBinaryLogClient.class);
        DatabaseBinlogOffsetKafkaStore binlogOffsetKafkaStore = SingletonServiceFactory.getBean(DatabaseBinlogOffsetKafkaStore.class);
        DebeziumBinlogOffsetKafkaStore debeziumBinlogOffsetKafkaStore = SingletonServiceFactory.getBean(DebeziumBinlogOffsetKafkaStore.class);
        return new MySQLCdcProcessor<>(mySqlBinaryLogClient, binlogOffsetKafkaStore, debeziumBinlogOffsetKafkaStore);
    }
}
