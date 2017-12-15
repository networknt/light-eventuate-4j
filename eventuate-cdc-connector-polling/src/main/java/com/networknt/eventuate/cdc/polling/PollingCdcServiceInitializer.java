package com.networknt.eventuate.cdc.polling;

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


public class PollingCdcServiceInitializer {

    public static String CDC_CONFIG_NAME = "cdc";
    public static CdcConfig cdcConfig = (CdcConfig) Config.getInstance().getJsonObjectConfig(CDC_CONFIG_NAME, CdcConfig.class);
    public static String KAFKA_CONFIG_NAME = "kafka";
    public static KafkaConfig kafkaConfig = (KafkaConfig) Config.getInstance().getJsonObjectConfig(KAFKA_CONFIG_NAME, KafkaConfig.class);

    public EventuateSchema eventuateSchema() {
        return new EventuateSchema();
    }



    public PollingDao pollingDao() {
        DataSource ds = (DataSource) SingletonServiceFactory.getBean(DataSource.class);
        EventPollingDataProvider  pollingDataProvider= (EventPollingDataProvider) SingletonServiceFactory.getBean(EventPollingDataProvider.class);

        return new PollingDao (pollingDataProvider, ds,
                cdcConfig.getMaxEventsPerPolling(),
                cdcConfig.getMaxAttemptsForPolling(),
                cdcConfig.getPollingRetryIntervalInMilliseconds());
    }

    public EventuateKafkaProducer eventuateKafkaProducer() {
        return new EventuateKafkaProducer();
    }



    public CdcProcessor<PublishedEvent> pollingCdcProcessor() {
        PollingDao pollingDao = SingletonServiceFactory.getBean(PollingDao.class);
         return new PollingCdcProcessor<>(pollingDao, cdcConfig.getPollingIntervalInMilliseconds());
    }

    public CdcKafkaPublisher<PublishedEvent> pollingCdcKafkaPublisher() {
        PublishingStrategy<PublishedEvent> publishingStrategy = SingletonServiceFactory.getBean(PublishingStrategy.class);
        return new PollingCdcKafkaPublisher<>( kafkaConfig.getBootstrapServers(), publishingStrategy);
    }

    public CuratorFramework curatorFramework() {
        String connectionString = cdcConfig.getZookeeper();
        return makeStartedCuratorClient(connectionString);
    }

    public EventTableChangesToAggregateTopicTranslator<PublishedEvent> pollingEventTableChangesToAggregateTopicTranslator() {
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
