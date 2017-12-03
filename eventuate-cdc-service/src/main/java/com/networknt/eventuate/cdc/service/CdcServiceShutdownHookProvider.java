package com.networknt.eventuate.cdc.service;

import com.networknt.server.ShutdownHookProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * cDc service ShutdownHookProvider, stop cDc service
 */
public class CdcServiceShutdownHookProvider implements ShutdownHookProvider {
    protected static Logger logger = LoggerFactory.getLogger(CdcServiceShutdownHookProvider.class);

    public void onShutdown() {
        if(CdcServiceStartupHookProvider.translator != null) {
            try {
                CdcServiceStartupHookProvider.translator.stop();
            } catch (Exception e) {
                logger.error("Exception: ", e);
            }
        }
        if(CdcServiceStartupHookProvider.curatorFramework != null) {
            CdcServiceStartupHookProvider.curatorFramework.close();
        }
        System.out.println("CdcServiceShutdownHookProvider is called");
    }
}
