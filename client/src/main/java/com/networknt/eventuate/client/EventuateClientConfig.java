package com.networknt.eventuate.client;

/**
 * Created by stevehu on 2016-11-27.
 */
public class EventuateClientConfig {
    String description;
    String handlerPackage;

    public EventuateClientConfig() {
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getHandlerPackage() {
        return handlerPackage;
    }

    public void setHandlerPackage(String handlerPackage) {
        this.handlerPackage = handlerPackage;
    }

}
