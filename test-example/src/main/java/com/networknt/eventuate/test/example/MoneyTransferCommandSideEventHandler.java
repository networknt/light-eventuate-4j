package com.networknt.eventuate.test.example;


import com.networknt.eventuate.common.EndOfCurrentEventsReachedEvent;
import com.networknt.eventuate.common.EventHandlerContext;
import com.networknt.eventuate.common.EventHandlerMethod;
import com.networknt.eventuate.common.EventSubscriber;
import com.networknt.eventuate.test.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@EventSubscriber(id="javaIntegrationTestCommandSideMoneyTransferEventHandlers",progressNotifications = true)
public class MoneyTransferCommandSideEventHandler {

  private Logger logger = LoggerFactory.getLogger(getClass());

  private EventTracker<EventHandlerContext<?>> events = EventTracker.create();

  public EventTracker<EventHandlerContext<?>> getEvents() {
    return events;
  }

  @EventHandlerMethod
  public void moneyTransferCreated(EventHandlerContext<MoneyTransferCreatedEvent> ctx) {
    logger.debug("moneyTransferCreated got event {}", ctx.getEventId());
    events.onNext(ctx);
  }

  @EventHandlerMethod
  public void doAnything(EventHandlerContext<AccountDebitedEvent> ctx) {
    logger.debug("doAnything got event {} {}", ctx.getEventId(), ctx.getEvent().getTransactionId());
    events.onNext(ctx);
  }

  @EventHandlerMethod
  public void noteProgress(EventHandlerContext<EndOfCurrentEventsReachedEvent> ctx) {
    logger.debug("noteProgress got event: " + ctx.getEvent());
    events.onNext(ctx);
  }

}

