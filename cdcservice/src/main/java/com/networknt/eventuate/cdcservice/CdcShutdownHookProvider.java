package com.networknt.eventuate.cdcservice;

import com.networknt.server.ShutdownHookProvider;

/**
 * cDc service ShutdownHookProvider, stop cDc service
 */
public class CdcShutdownHookProvider implements ShutdownHookProvider {
    public void onShutdown() {
        if(CdcStartupHookProvider.curatorFramework != null) {
            CdcStartupHookProvider.curatorFramework.close();
        }
        System.out.println("CdcShutdownHookProvider is called");
    }
}
