package com.networknt.eventuate.test.example;


import com.networknt.eventuate.common.EventHandlerContext;
import com.networknt.eventuate.common.EventHandlerMethod;
import com.networknt.eventuate.common.EventSubscriber;
import com.networknt.eventuate.test.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

@EventSubscriber(id="javaIntegrationTestCommandSideAccountEventHandlers")
public class AccountCommandSideEventHandler {

  private Logger logger = LoggerFactory.getLogger(getClass());

  private EventTracker<EventHandlerContext<?>> events = EventTracker.create();

  public EventTracker<EventHandlerContext<?>> getEvents() {
    return events;
  }

  @EventHandlerMethod
  public void doAnything(EventHandlerContext<AccountCreatedEvent> ctx) {
    events.onNext(ctx);
  }

  @EventHandlerMethod
  public CompletableFuture<?> debitAccount(EventHandlerContext<MoneyTransferCreatedEvent> ctx) {
    events.onNext(ctx);
    logger.debug("debiting account");
    TransferDetails details = ctx.getEvent().getDetails();
    return ctx.update(Account.class, details.getFromAccountId(),
            new DebitAccountCommand(details.getAmount(), ctx.getEntityId()));
  }
}

