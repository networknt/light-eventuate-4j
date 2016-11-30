package com.networknt.eventuate.test.example;

/*
import io.eventuate.AggregateRepository;
import io.eventuate.EventuateAggregateStore;
import io.eventuate.example.banking.domain.Account;
import io.eventuate.example.banking.domain.AccountCommand;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
*/

//@Configuration
public class JavaIntegrationTestDomainConfiguration {
    /*
  @Bean
  public AccountCommandSideEventHandler accountCommandSideEventHandler() {
    return new AccountCommandSideEventHandler();
  }

  @Bean
  public MoneyTransferCommandSideEventHandler moneyTransferCommandSideEventHandler() {
    return new MoneyTransferCommandSideEventHandler();
  }

 @Bean
  public AccountQuerySideEventHandler accountQuerySideEventHandler() {
    return new AccountQuerySideEventHandler();
  }


  @Bean
  public AccountService accountService(AggregateRepository<Account, AccountCommand> accountRepository) {
    return new AccountService(accountRepository);
  }

  @Bean
  public AggregateRepository<Account, AccountCommand> accountRepository(EventuateAggregateStore aggregateStore) {
    return new AggregateRepository<Account, AccountCommand>(Account.class, aggregateStore);
  }
    */
}
