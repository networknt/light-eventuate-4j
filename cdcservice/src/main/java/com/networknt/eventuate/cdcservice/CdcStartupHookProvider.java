package com.networknt.eventuate.cdcservice;

import com.networknt.config.Config;
import com.networknt.eventuate.debezium.EventTableChangesToAggregateTopicRelay;
import com.networknt.eventuate.debezium.EventTableChangesToAggregateTopicRelayMysql;
import com.networknt.eventuate.debezium.EventTableChangesToAggregateTopicRelayPostgres;
import com.networknt.eventuate.debezium.TableChangeToTopicRelay;
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
    static String DB_TYPE_POSTGRE_SQL = "postgres";

    static CdcServiceConfig config;
    static TableChangeToTopicRelay relay;
    public static CuratorFramework curatorFramework;

    public void onStartup() {
        config = (CdcServiceConfig) Config.getInstance().getJsonObjectConfig(CONFIG_NAME, CdcServiceConfig.class);
        curatorFramework = makeStartedCuratorClient(config.getZookeeper());
        if (DB_TYPE_POSTGRE_SQL.equalsIgnoreCase(config.getDbType())) {
             relay = new EventTableChangesToAggregateTopicRelayPostgres(config.getKafka(), config.getPostgresDbHost(), config.getPostgresDBPort(),
                    config.getPostgresDbUser(), config.getPostgresDbPass(), config.getPostgresDbName(),  curatorFramework);

        } else {
             relay = new EventTableChangesToAggregateTopicRelayMysql(config.getKafka(), config.dbHost, config.dbPort,
                    config.dbUser, config.dbPass, config.dbName, curatorFramework);
        }

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
