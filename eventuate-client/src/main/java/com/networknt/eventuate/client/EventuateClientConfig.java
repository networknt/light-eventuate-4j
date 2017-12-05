package com.networknt.eventuate.client;

/**
 * eventuate client package Configuration class. It is used to control the scope of the
 * classpath scanner to the package that contains the handlers. Otherwise, all classes
 * in the classpath will be scanned.
 *
 */
public class EventuateClientConfig {
    String handlerPackage;

    public EventuateClientConfig() {
    }

    public String getHandlerPackage() {
        return handlerPackage;
    }

    public void setHandlerPackage(String handlerPackage) {
        this.handlerPackage = handlerPackage;
    }

}
