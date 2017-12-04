package com.networknt.eventuate.cdc.server;

import com.networknt.eventuate.server.common.EventTableChangesToAggregateTopicTranslator;
import com.networknt.eventuate.server.common.PublishedEvent;
import com.networknt.server.StartupHookProvider;
import com.networknt.service.SingletonServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CdcServer StartupHookProvider. start cdc service
 */
public class CdcServerStartupHookProvider implements StartupHookProvider {
    private static final Logger logger = LoggerFactory.getLogger(CdcServerStartupHookProvider.class);

    public void onStartup() {
        EventTableChangesToAggregateTopicTranslator<PublishedEvent> translator = SingletonServiceFactory.getBean(EventTableChangesToAggregateTopicTranslator.class);
        translator.start();
        logger.info("CdcServerStartupHookProvider is called");
    }
}
