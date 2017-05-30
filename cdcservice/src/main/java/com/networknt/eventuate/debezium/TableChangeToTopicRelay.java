package com.networknt.eventuate.debezium;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * cds API for handle database change.  Turns the existing databases into event streams,
 */
public interface TableChangeToTopicRelay {

    @PostConstruct
    public void start();

    @PreDestroy
    public void stop() throws InterruptedException;
}
