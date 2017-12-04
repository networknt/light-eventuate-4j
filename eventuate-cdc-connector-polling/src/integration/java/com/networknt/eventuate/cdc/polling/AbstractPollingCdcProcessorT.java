package com.networknt.eventuate.cdc.polling;

import com.networknt.eventuate.server.common.CdcProcessor;
import com.networknt.eventuate.server.common.PublishedEvent;
import com.networknt.eventuate.server.test.util.CdcProcessorTest;
import com.networknt.service.SingletonServiceFactory;

public abstract class AbstractPollingCdcProcessorT extends CdcProcessorTest {

  private PollingDao<PublishedEventBean, PublishedEvent, String> pollingDao = SingletonServiceFactory.getBean(PollingDao.class);

  @Override
  protected CdcProcessor<PublishedEvent> createCdcProcessor() {
    return new PollingCdcProcessor<>(pollingDao, 500);
  }
}
