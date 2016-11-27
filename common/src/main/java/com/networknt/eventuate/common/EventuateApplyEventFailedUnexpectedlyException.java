package com.networknt.eventuate.common;

public class EventuateApplyEventFailedUnexpectedlyException extends EventuateClientException {

  public EventuateApplyEventFailedUnexpectedlyException(ReflectiveOperationException e) {
    super(e);
  }

}
