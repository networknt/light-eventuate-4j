package com.networknt.eventuate.test;

import com.networknt.eventuate.test.domain.*;
import com.networknt.eventuate.common.*;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class AggregateRepositoryTest {

  private AggregateRepository<Account, AccountCommand> repository;
  private EventuateAggregateStore aggregateStore;

  private final static BigDecimal INITIAL_BALANCE = new BigDecimal(1234);
  private final static BigDecimal DEBIT_AMOUNT = new BigDecimal(10);
  private final static BigDecimal POST_DEBIT_BALANCE = INITIAL_BALANCE.subtract(DEBIT_AMOUNT);

  private final String entityId = "entityId1234";
  private final Int128 createdEntityVersion = new Int128(1, 2);
  private final EntityIdAndVersion entityIdAndVersion = new EntityIdAndVersion(entityId, createdEntityVersion);
  private final List<Event> creationEvents = Collections.singletonList(new AccountCreatedEvent(INITIAL_BALANCE));

  private final Int128 updatedEntityVersion = new Int128(3, 4);
  private final EntityIdAndVersion entityIdAndUpdatedVersion = new EntityIdAndVersion(entityId, updatedEntityVersion);

  private final Int128 updatedTwiceEntityVersion = new Int128(6, 7);

  private final String transaction1234 = "transaction1234";
  List<Event> debitedEvents = Collections.singletonList(new AccountDebitedEvent(DEBIT_AMOUNT, transaction1234));

  private List<Event> creationAndUpdateEvents = Stream.concat(creationEvents.stream(), debitedEvents.stream()).collect(Collectors.toList());
  private final String eventToken = "eventId1234";
  private EventContext triggeringEventContext = new EventContext(eventToken);
  private Account accountToUpdate;
  private final Optional<UpdateOptions> UPDATE_OPTIONS_WITH_TRIGGERING_EVENT = Optional.of(new UpdateOptions().withTriggeringEvent(triggeringEventContext));
  private final Optional<FindOptions> FIND_OPTIONS_WITH_TRIGGERING_EVENT = Optional.of(new FindOptions().withTriggeringEvent(triggeringEventContext));


  @Before
  public void setUp() {
    aggregateStore = mock(EventuateAggregateStore.class);
    repository = new AggregateRepository<>(Account.class, aggregateStore);
    accountToUpdate = new Account();
    accountToUpdate.apply(new AccountCreatedEvent(INITIAL_BALANCE));
  }

  @Test
  public void shouldSave() throws ExecutionException, InterruptedException {
    when(aggregateStore.save(Account.class, creationEvents, Optional.empty()))
            .thenReturn(CompletableFuture.completedFuture(entityIdAndVersion));

    EntityWithIdAndVersion<Account> ewidv = repository.save(new CreateAccountCommand(INITIAL_BALANCE), Optional.empty()).get();

    assertEquals(entityId, ewidv.getEntityIdAndVersion().getEntityId());
    assertEquals(createdEntityVersion, ewidv.getEntityIdAndVersion().getEntityVersion());
    assertEquals(INITIAL_BALANCE, ewidv.getAggregate().getBalance());

    verify(aggregateStore).save(Account.class, creationEvents, Optional.empty());

    verifyNoMoreInteractions(aggregateStore);

  }

  @Test
  public void shouldUpdate() throws ExecutionException, InterruptedException {

    when(aggregateStore.find(Account.class, entityId, Optional.empty()))
            .thenReturn(CompletableFuture.completedFuture(new EntityWithMetadata<>(entityIdAndVersion, creationEvents, accountToUpdate)));


    when(aggregateStore.update(Account.class, entityIdAndVersion,
            debitedEvents, Optional.empty()))
            .thenReturn(CompletableFuture.completedFuture(new EntityIdAndVersion(entityId, updatedEntityVersion)));

    EntityWithIdAndVersion<Account> ewidv = repository.update(entityId,
            new DebitAccountCommand(DEBIT_AMOUNT, transaction1234), Optional.empty()).get();

    assertEquals(entityId, ewidv.getEntityIdAndVersion().getEntityId());
    assertEquals(updatedEntityVersion, ewidv.getEntityIdAndVersion().getEntityVersion());
    assertEquals(POST_DEBIT_BALANCE, ewidv.getAggregate().getBalance());

    verify(aggregateStore).find(Account.class, entityId, Optional.empty());
    verify(aggregateStore).update(Account.class, entityIdAndVersion,
            debitedEvents, Optional.empty());

    verifyNoMoreInteractions(aggregateStore);

  }
  @Test
  public void shouldUpdateWhenNoEvents() throws ExecutionException, InterruptedException {

    when(aggregateStore.find(Account.class, entityId, Optional.empty()))
            .thenReturn(CompletableFuture.completedFuture(new EntityWithMetadata<>(entityIdAndVersion, creationEvents, accountToUpdate)));

    EntityWithIdAndVersion<Account> ewidv = repository.update(entityId,
            new NoopAccountCommand(), Optional.empty()).get();

    assertEquals(entityId, ewidv.getEntityIdAndVersion().getEntityId());
    assertEquals(createdEntityVersion, ewidv.getEntityIdAndVersion().getEntityVersion());
    assertEquals(INITIAL_BALANCE, ewidv.getAggregate().getBalance());

    verify(aggregateStore).find(Account.class, entityId, Optional.empty());

    verifyNoMoreInteractions(aggregateStore);

  }

  @Test
  public void shouldUpdateWithOptimisticLockingFailure() throws ExecutionException, InterruptedException {

    when(aggregateStore.find(Account.class, entityId, Optional.empty()))
            .thenReturn(
                    CompletableFuture.completedFuture(new EntityWithMetadata<>(entityIdAndVersion, creationEvents, accountToUpdate)),
                    CompletableFuture.completedFuture(new EntityWithMetadata<>(entityIdAndUpdatedVersion, creationAndUpdateEvents, accountToUpdate))
                    );


    when(aggregateStore.update(Account.class, entityIdAndVersion,
            debitedEvents, Optional.empty()))
            .thenReturn(CompletableFutureUtil.failedFuture(new OptimisticLockingException()));


    when(aggregateStore.update(Account.class, entityIdAndUpdatedVersion,
            debitedEvents, Optional.empty()))
            .thenReturn(CompletableFuture.completedFuture(new EntityIdAndVersion(entityId, updatedTwiceEntityVersion)));

    CompletableFuture<EntityWithIdAndVersion<Account>> updateF = repository.update(entityId,
            new DebitAccountCommand(DEBIT_AMOUNT, transaction1234), Optional.empty());
    EntityWithIdAndVersion<Account> ewidv = updateF.get();

    assertEquals(entityId, ewidv.getEntityIdAndVersion().getEntityId());
    assertEquals(updatedTwiceEntityVersion, ewidv.getEntityIdAndVersion().getEntityVersion());
    assertEquals(POST_DEBIT_BALANCE, ewidv.getAggregate().getBalance());

    verify(aggregateStore, times(2)).find(Account.class, entityId, Optional.empty());

    verify(aggregateStore).update(Account.class, entityIdAndVersion,
            debitedEvents, Optional.empty());
    verify(aggregateStore).update(Account.class, entityIdAndUpdatedVersion,
            debitedEvents, Optional.empty());

    verifyNoMoreInteractions(aggregateStore);

  }


  @Test
  public void shouldUpdateWithDuplicateTriggeringEventExceptionThrownByUpdate() throws ExecutionException, InterruptedException {

    when(aggregateStore.find(Account.class, entityId, FIND_OPTIONS_WITH_TRIGGERING_EVENT))
            .thenReturn(
                    CompletableFuture.completedFuture(new EntityWithMetadata<>(entityIdAndVersion, creationEvents, accountToUpdate)));

    when(aggregateStore.update(Account.class, entityIdAndVersion,
            debitedEvents, UPDATE_OPTIONS_WITH_TRIGGERING_EVENT))
            .thenReturn(CompletableFutureUtil.failedFuture(new DuplicateTriggeringEventException()));

    when(aggregateStore.find(Account.class, entityId, Optional.empty()))
            .thenReturn(
                    CompletableFuture.completedFuture(new EntityWithMetadata<>(entityIdAndVersion, creationEvents, accountToUpdate)));

    EntityWithIdAndVersion<Account> ewidv = repository.update(entityId,
            new DebitAccountCommand(DEBIT_AMOUNT, transaction1234), UPDATE_OPTIONS_WITH_TRIGGERING_EVENT).get();

    assertEquals(entityId, ewidv.getEntityIdAndVersion().getEntityId());
    assertEquals(createdEntityVersion, ewidv.getEntityIdAndVersion().getEntityVersion());
    assertEquals(INITIAL_BALANCE, ewidv.getAggregate().getBalance());

    verify(aggregateStore).find(Account.class, entityId, FIND_OPTIONS_WITH_TRIGGERING_EVENT);
    verify(aggregateStore).update(Account.class, entityIdAndVersion,
            debitedEvents, UPDATE_OPTIONS_WITH_TRIGGERING_EVENT);

    verifyNoMoreInteractions(aggregateStore);


  }

  @Test
  public void shouldUpdateWithDuplicateTriggeringEventExceptionThrownByFind() throws ExecutionException, InterruptedException {

    when(aggregateStore.find(Account.class, entityId, FIND_OPTIONS_WITH_TRIGGERING_EVENT))
            .thenReturn(CompletableFutureUtil.failedFuture(new DuplicateTriggeringEventException()));

    when(aggregateStore.find(Account.class, entityId, Optional.empty()))
            .thenReturn(
                    CompletableFuture.completedFuture(new EntityWithMetadata<>(entityIdAndVersion, creationEvents, accountToUpdate)));

    EntityWithIdAndVersion<Account> ewidv = repository.update(entityId,
            new DebitAccountCommand(DEBIT_AMOUNT, transaction1234), UPDATE_OPTIONS_WITH_TRIGGERING_EVENT).get();

    assertEquals(entityId, ewidv.getEntityIdAndVersion().getEntityId());
    assertEquals(createdEntityVersion, ewidv.getEntityIdAndVersion().getEntityVersion());
    assertEquals(INITIAL_BALANCE, ewidv.getAggregate().getBalance());

    verify(aggregateStore).find(Account.class, entityId, FIND_OPTIONS_WITH_TRIGGERING_EVENT);
    verify(aggregateStore).find(Account.class, entityId, Optional.empty());

    verifyNoMoreInteractions(aggregateStore);


  }


}
