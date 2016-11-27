package com.networknt.eventuate.cdcservice;

import com.networknt.server.ShutdownHookProvider;

/**
 * Created by stevehu on 2016-11-22.
 */
public class CdcShutdownHookProvider implements ShutdownHookProvider {
    public void onShutdown() {
        if(CdcStartupHookProvider.curatorFramework != null) {
            CdcStartupHookProvider.curatorFramework.close();
        }
        System.out.println("CdcShutdownHookProvider is called");
    }
}
