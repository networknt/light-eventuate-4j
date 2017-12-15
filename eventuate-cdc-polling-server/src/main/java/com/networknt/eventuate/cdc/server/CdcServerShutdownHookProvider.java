package com.networknt.eventuate.cdc.server;

import com.networknt.eventuate.server.common.EventTableChangesToAggregateTopicTranslator;
import com.networknt.eventuate.server.common.PublishedEvent;
import com.networknt.server.ShutdownHookProvider;
import com.networknt.service.SingletonServiceFactory;
import org.apache.curator.framework.CuratorFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * cDc service ShutdownHookProvider, stop cDc service
 */
public class CdcServerShutdownHookProvider implements ShutdownHookProvider {
    private static final Logger logger = LoggerFactory.getLogger(CdcServerShutdownHookProvider.class);

    public void onShutdown() {
        EventTableChangesToAggregateTopicTranslator<PublishedEvent> translator = SingletonServiceFactory.getBean(EventTableChangesToAggregateTopicTranslator.class);
        if(translator != null) {
            try {
                translator.stop();
            } catch (Exception e) {
                logger.error("Exception: ", e);
            }
        }
        CuratorFramework curatorFramework = SingletonServiceFactory.getBean(CuratorFramework.class);
        if(curatorFramework != null) {
            curatorFramework.close();
        }
        logger.info("CdcServerShutdownHookProvider is called");
    }
}
