package com.networknt.eventuate.test.jdbc;

import com.networknt.eventuate.common.EntityIdAndVersion;
import com.networknt.eventuate.common.EntityWithMetadata;
import com.networknt.eventuate.common.Event;
import com.networknt.eventuate.common.EventuateAggregateStore;
import com.networknt.eventuate.common.impl.EventuateAggregateStoreImpl;
import com.networknt.eventuate.test.domain.Account;
import com.networknt.eventuate.test.domain.CreateAccountCommand;
import com.networknt.service.SingletonServiceFactory;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

public class IdempotencyTest {

  public static EventuateAggregateStoreImpl aggregateStore = (EventuateAggregateStoreImpl)SingletonServiceFactory.getBean(EventuateAggregateStore.class);

  @Test
  public void secondUpdateShouldBeRejected() throws ExecutionException, InterruptedException {

    Account account = new Account();
    List<Event> accountEvents = account.process(new CreateAccountCommand(new BigDecimal(12345)));

    EntityIdAndVersion savedAccountEntity = aggregateStore.save(Account.class, accountEvents, Optional.empty()).get();

    EntityWithMetadata<Account> loadedAccount = aggregateStore.find(Account.class, savedAccountEntity.getEntityId(), Optional.empty()).get();
  }
}
