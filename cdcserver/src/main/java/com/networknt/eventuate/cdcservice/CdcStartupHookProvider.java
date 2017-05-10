package com.networknt.eventuate.cdcservice;

import com.networknt.config.Config;
import com.networknt.eventuate.debezium.EventTableChangesToAggregateTopicRelay;
import com.networknt.server.StartupHookProvider;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

/**
 * CdcService StartupHookProvider. start cdc service
 */
public class CdcStartupHookProvider implements StartupHookProvider {

    static String CONFIG_NAME = "cdcservice";
    static CdcServiceConfig config;
    static EventTableChangesToAggregateTopicRelay relay;
    public static CuratorFramework curatorFramework;

    public void onStartup() {
        config = (CdcServiceConfig) Config.getInstance().getJsonObjectConfig(CONFIG_NAME, CdcServiceConfig.class);
        curatorFramework = makeStartedCuratorClient(config.getZookeeper());
        relay = new EventTableChangesToAggregateTopicRelay(config.getKafka(), config.dbHost, config.dbPort,
                config.dbUser, config.dbPass, curatorFramework);
        relay.start();
        System.out.println("CdcStartupHookProvider is called");
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
