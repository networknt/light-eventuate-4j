package com.networknt.eventuate.eventhandling.exceptionhandling;

public interface EventDeliveryExceptionHandlerManager {

  EventDeliveryExceptionHandlerWithState getEventHandler(Throwable t);


}
