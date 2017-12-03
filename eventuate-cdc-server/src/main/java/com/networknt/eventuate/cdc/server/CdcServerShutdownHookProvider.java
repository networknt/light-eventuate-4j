package com.networknt.eventuate.cdc.server;

import com.networknt.server.ShutdownHookProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * cDc service ShutdownHookProvider, stop cDc service
 */
public class CdcServerShutdownHookProvider implements ShutdownHookProvider {
    protected static Logger logger = LoggerFactory.getLogger(CdcServerShutdownHookProvider.class);

    public void onShutdown() {
        if(CdcServerStartupHookProvider.translator != null) {
            try {
                CdcServerStartupHookProvider.translator.stop();
            } catch (Exception e) {
                logger.error("Exception: ", e);
            }
        }

        if(CdcServerStartupHookProvider.curatorFramework != null) {
            CdcServerStartupHookProvider.curatorFramework.close();
        }
        System.out.println("CdcServerShutdownHookProvider is called");
    }
}
