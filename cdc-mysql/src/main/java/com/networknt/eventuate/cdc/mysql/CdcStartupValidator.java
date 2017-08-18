package com.networknt.eventuate.cdc.mysql;


import com.zaxxer.hikari.HikariDataSource;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.KafkaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

public class CdcStartupValidator {

    private String jdbcUrl;
    private String dbUser;
    private String dbPassword;
    private long mySqlValidationTimeoutMillis = 1000;
    private int mySqlValidationMaxAttempts = 20;
    private long kafkaValidationTimeoutMillis = 1000;
    private int kafkaValidationMaxAttempts = 20;

    private String bootstrapServers;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public CdcStartupValidator(String jdbcUrl, String dbUser, String dbPassword, String bootstrapServers) {
        this.jdbcUrl = jdbcUrl;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;
        this.bootstrapServers = bootstrapServers;
    }

    public void validateEnvironment() {
        validateDatasourceConnection();
        validateKafkaConnection();
    }

    private void validateDatasourceConnection() {
        logger.info("About to validate DataSource connection");

        HikariDataSource dataSource;

        Exception lastException = null;
        int i = mySqlValidationMaxAttempts;

        while (i > 0) {
            try {
                dataSource = new HikariDataSource();

                dataSource.setJdbcUrl(this.jdbcUrl);
                dataSource.setUsername(this.dbUser);
                dataSource.setPassword(this.dbPassword);
                dataSource.setDriverClassName("com.mysql.jdbc.Driver");
                final Connection connection = dataSource.getConnection();
                PreparedStatement stmt = connection.prepareStatement("SELECT version()");
                stmt.executeQuery();
                logger.info("Successfully tested connection for {}:{} with user '{}'", this.jdbcUrl);
                return;
            }
            catch (Exception e) {
                lastException = e;
                logger.info("Failed testing connection for {}:{} with user '{}'", this.jdbcUrl);
                i--;
                try {
                    Thread.sleep(mySqlValidationTimeoutMillis);
                } catch (InterruptedException ie) {
                    throw new RuntimeException("MySql validation had been interrupted!", ie);
                }
            }

            mySqlValidationTimeoutMillis = mySqlValidationTimeoutMillis * 2;
        }
        throw new RuntimeException(lastException);
     }

    private void validateKafkaConnection() {
        KafkaConsumer<String, String>  consumer = getTestConsumer();

        int i = kafkaValidationMaxAttempts;
        KafkaException lastException = null;
        while (i > 0) {
            try {
                consumer.listTopics();
                logger.info("Successfully tested Kafka connection");
                return;
            } catch (KafkaException e) {
                logger.info("Failed to connection to Kafka");
                lastException = e;
                i--;
                try {
                    Thread.sleep(kafkaValidationTimeoutMillis);
                } catch (InterruptedException ie) {
                    throw new RuntimeException("Kafka validation had been interrupted!", ie);
                }
            }
            kafkaValidationTimeoutMillis = kafkaValidationTimeoutMillis * 2;
        }
        throw lastException;
    }

    private KafkaConsumer<String, String> getTestConsumer() {
        Properties consumerProperties = new Properties();
        consumerProperties.put("bootstrap.servers", bootstrapServers);
        consumerProperties.put("group.id", "stratup-test-subscriber");
        consumerProperties.put("request.timeout.ms", String.valueOf(kafkaValidationTimeoutMillis + 1));
        consumerProperties.put("session.timeout.ms", String.valueOf(kafkaValidationTimeoutMillis));
        consumerProperties.put("heartbeat.interval.ms", String.valueOf(kafkaValidationTimeoutMillis - 1));
        consumerProperties.put("fetch.max.wait.ms", String.valueOf(kafkaValidationTimeoutMillis));
        consumerProperties.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        consumerProperties.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

        return new KafkaConsumer<>(consumerProperties);
    }


    public void setMySqlValidationTimeoutMillis(long mySqlValidationTimeoutMillis) {
        this.mySqlValidationTimeoutMillis = mySqlValidationTimeoutMillis;
    }

    public void setMySqlValidationMaxAttempts(int mySqlValidationMaxAttempts) {
        this.mySqlValidationMaxAttempts = mySqlValidationMaxAttempts;
    }

    public void setKafkaValidationTimeoutMillis(long kafkaValidationTimeoutMillis) {
        this.kafkaValidationTimeoutMillis = kafkaValidationTimeoutMillis;
    }

    public void setKafkaValidationMaxAttempts(int kafkaValidationMaxAttempts) {
        this.kafkaValidationMaxAttempts = kafkaValidationMaxAttempts;
    }
}