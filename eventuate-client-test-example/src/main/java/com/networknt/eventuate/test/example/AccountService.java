package com.networknt.eventuate.test.example;


import com.networknt.eventuate.common.AggregateRepository;
import com.networknt.eventuate.common.EntityWithIdAndVersion;
import com.networknt.eventuate.test.domain.*;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

public class AccountService {
  private final AggregateRepository<Account, AccountCommand> accountRepository;

  public AccountService(AggregateRepository<Account, AccountCommand> accountRepository) {
    this.accountRepository = accountRepository;
  }

  public CompletableFuture<EntityWithIdAndVersion<Account>> openAccount(BigDecimal initialBalance) {
    return accountRepository.save(new CreateAccountCommand(initialBalance));
  }
}
