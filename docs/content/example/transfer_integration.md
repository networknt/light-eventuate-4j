---
date: 2016-10-22T20:22:34-04:00
title: Account Money Transfer
---




# Integration Test Setting


## Build related projects:

Checkout light-eventuate-4j framework projects.

cd ~/networknt


git clone git@github.com:networknt/light-eventuate-4j.git


## Prepare workspace
Go into the projects folder above, and build the project with maven


mvn clean install



## Build Account-management example

Get the example project from github:
git clone git@github.com:networknt/light-eventuate-example.git

cd ~/networknt/light-eventuate-example/account-management

mvn clean install


## Run the Event store and Microservices:

First we need to make sure Mysql, Zookeeper, Kafka and CDC server are up and running.

You can follow this [tutorial](https://networknt.github.io/light-eventuate-4j/tutorial/service-dev/)
to start all of them with docker-compose.

Then start service docker compose file for account money transfer services

cd ~/networknt/light-eventuate-example/account-management
docker-compose down
docker-compose up



# Test and verify result:



## From command line:

## Create new customer (C1):

* On customer command side, system sent the cCreateCustomerCommand and apply CustomerCreatedEvent event.
* On customer view side, system subscrible the  CustomerCreatedEvent by registered event handles. On the example, system will process event and save customer to local database.


```
curl -X POST \
  http://localhost:8083/v1/createcustomer \
  -H 'cache-control: no-cache' \
  -H 'content-type: application/json' \
  -d '{"name":{"firstName":"Google11","lastName":"Com"},"email":"aaa1.bbb1@google.com","password":"password","ssn":"9999999999","phoneNumber":"4166666666","address":{"street1":"Yonge St","street2":"2556 unit","city":"toronto","state":"ON","zipCode":"Canada","country":"L3R 5F5"}}'

  Sample Result:

  {"id":"0000015cf50351d8-0242ac1200060000","customerInfo":{"name":{"firstName":"Google22","lastName":"Com"},"email":"aaa1.bbb@google.com","password":"password","ssn":"9999999999","phoneNumber":"4166666666","address":{"street1":"Yonge St","street2":"2556 unit","city":"toronto","state":"ON","zipCode":"Canada","country":"L3R 5F5"}}}
```



## Create an account with the customer (replace the customer id with real customer id):

* On account command side, system sent the OpenAccountCommand  and apply AccountOpenedEvent event.
* On account view side, system subscrible the  AccountOpenedEvent by registered event handles. On the example, system will process event and save account and account/customer relationship to local database.

```
curl -X POST \
  http://localhost:8081/v1/openaccount \
  -H 'cache-control: no-cache' \
  -H 'content-type: application/json' \
  -d '{"customerId":"0000015cf50351d8-0242ac1200060000","title":"RRSP account","description":"account for testing","initialBalance":12355}'
```

Result:

{"accountId":"0000015cf505ed48-0242ac1200090001","balance":12355}



## Create an account (no link with customer)

* On account command side, system sent the OpenAccountCommand  and apply AccountOpenedEvent event.
* On account view side, system subscrible the  AccountOpenedEvent by registered event handles. On the example, system will process event and save account to local database.

```
curl -X POST \
  http://localhost:8081/v1/openaccount \
  -H 'cache-control: no-cache' \
  -H 'content-type: application/json' \
  -d '{"title":"RRSP account","description":"account for testing","initialBalance":12355}'
```

Result:

```
{"accountId":"0000015cf5084627-0242ac1200090001","balance":12355}
```

## Create new customer (C2):

* On customer command side, system sent the CreateCustomerCommand and apply CustomerCreatedEvent event.
* On customer view side, system subscrible the  CustomerCreatedEvent by registered event handles. On the example, system will process event and save customer to local database.

```
curl -X POST \
  http://localhost:8083/v1/createcustomer \
  -H 'cache-control: no-cache' \
  -H 'content-type: application/json' \
  -d '{"name":{"firstName":"Google11","lastName":"Com"},"email":"aaa2.bbb@google.com","password":"password","ssn":"9999999999","phoneNumber":"4166666666","address":{"street1":"Yonge St","street2":"2556 unit","city":"toronto","state":"ON","zipCode":"Canada","country":"L3R 5F5"}}'
```

Result:
```
{"id":"0000015cf50bfe50-0242ac1200060001","customerInfo":{"name":{"firstName":"Google11","lastName":"Com"},"email":"aaa2.bbb@google.com","password":"password","ssn":"9999999999","phoneNumber":"4166666666","address":{"street1":"Yonge St","street2":"2556 unit","city":"toronto","state":"ON","zipCode":"Canada","country":"L3R 5F5"}}}
```


## Link account to customer (replace the customer id and account with real Id):

* On customer command side, system sent the AddToAccountCommand and apply CustomerAddedToAccount event.
* On customer view side, system subscrible the  CustomerAddedToAccount event by registered event handles. On the example, system will process event and save customer/account relationship to local database.

```
curl -X POST \
  http://localhost:8083/v1/customers/toaccounts/{customerId} \
  -H 'cache-control: no-cache' \
  -H 'content-type: application/json' \
  -d '{"id":"0000015cf5084627-0242ac1200090001","title":"title","owner":"google","description":"test case"}'
```

Result: 0000015cf50bfe50-0242ac1200060001


## Transfer money from account (replace the from account and to account id with real id):
 
* On transaction command side, system send MoneyTransferCommand and apply the MoneyTransferCreatedEvent event with certain amount
* On account command side, system subscrible the MoneyTransferCreatedEvent event. System will verify the account balance based on the debit event.
* If the balance is not enough, system publish AccountDebitFailedDueToInsufficientFundsEvent. Otherwise, system send AccountCreditedEvent/AccountDebitedEvent.
* On transaction command side, if subscribed events are AccountCreditedEvent/AccountDebitedEvent, system will process event and publish CreditRecordedEvent/debitRecordedevent,
  if subscribed event is AccountDebitFailedDueToInsufficientFundsEvent, system will publish FailedDebitRecordedEvent.
* On account view side, if subscribed events are creditRecorded event and debitRecorded event, system will update local account balance and update the transaction status to COMPLETED.
  if subscribed even FailedDebitRecordedEvent, system will update transaction status to FAILED_DUE_TO_INSUFFICIENT_FUNDS.

```
curl -X POST \
  http://localhost:8085/v1/transfers \
  -H 'cache-control: no-cache' \
  -H 'content-type: application/json' \
  -d '{"fromAccountId":"0000015cf505ed48-0242ac1200090001","toAccountId":"0000015cf5084627-0242ac1200090001","amount":5000,"description":"test"}'
```

Result:

```
{"moneyTransferId":"0000015cf5118e64-0242ac1200080000"}
```


## Delete account:

* On account command side, system sent the DeleteAccountCommand  and apply AccountDeletedEvent event.
* On account view side, system subscrible the  AccountDeletedEvent by registered event handles. On the example, system will process event and inactive account to local database.
* On customer view side, system subscrible the  AccountDeletedEvent event by registered event handles. On the example, system will process event and delete customer/account relationship to local database.

```
curl -X DELETE \
  http://localhost:8081/v1/delete/0000015cf4bec29b-0242ac1200070001 \
  -H 'cache-control: no-cache' \
  -H 'content-type: application/json' \
```



## Customer and Account view:

--View the new customer by email (provide wildcard search):

```
  http://localhost:8084/v1/customers/aaa1.bbb1@google.com
```

Result:

```
{"customers":[{"id":"0000015cf50351d8-0242ac1200060000","name":{"firstName":"Google22","lastName":"Com"},"email":"aaa1.bbb@google.com","password":"password","ssn":"9999999999","phoneNumber":"4166666666","address":{"street1":"Yonge St","street2":"2556 unit","city":"toronto","state":"ON","zipCode":"Canada","country":"L3R 5F5"},"toAccounts":null}]}
```


--View the new customer by customer Id (replace the customer id with real customer id,. You can use copy from result of create customer)

```
  http://localhost:8084/v1/customer/{customerId}
```

Result:

```
{"id":"0000015cf50351d8-0242ac1200060000","name":{"firstName":"Google22","lastName":"Com"},"email":"aaa1.bbb@google.com","password":"password","ssn":"9999999999","phoneNumber":"4166666666","address":{"street1":"Yonge St","street2":"2556 unit","city":"toronto","state":"ON","zipCode":"Canada","country":"L3R 5F5"},"toAccounts":null}
```

-- view account by Id (replace the Id with real account Id)

http://localhost:8082/v1/accounts/{accountId}

Result:

```
{"accountId":"0000015cf505ed48-0242ac1200090001","balance":73550000,"title":"RRSP account","description":"account for testing"}
```

-- View account transaction history:

```
http://localhost:8082/v1/accounts/0000015cf910e31b-0242ac1200080000/history
```

Result:

```
[{"date":"2017-06-30","entryType":null,"transactionId":"0000015cf914b22e-0242ac1200070001","fromAccountId":"0000015cf910e31b-0242ac1200080000","toAccountId":"0000015cf9115782-0242ac1200080001","amount":50000000,"description":"test","status":"COMPLETED"}]
```






## Use Postman:


## Create new customer (C1):

POST URL: http://localhost:8083/v1/createcustomer

Header: Content-Type: application/json

Body:
```
{"name":{"firstName":"Google1222","lastName":"Com1"},"email":"aaa1.bbb1@google.com","password":"password","ssn":"9999999999","phoneNumber":"4166666666","address":{"street1":"Yonge St","street2":"2556 unit","city":"toronto","state":"ON","zipCode":"Canada","country":"L3R 5F5"}}
```

## Create an account with the customer (replace the customer id with real customer id):

POST URL: http://localhost:8081/v1/openaccount

Header: Content-Type: application/json

Body: {"customerId":"0000015cf4be6114-0242ac1200060001","title":"RRSP account","description":"account for testing","initialBalance":12355}


## Create an account (no link with customer)

POST URL: http://localhost:8081/v1/openaccount

Header: Content-Type: application/json

Body: {"title":"RRSP account","description":"account for testing","initialBalance":12355}


## Create new customer (C2):

POST URL: http://localhost:8083/v1/createcustomer

Header: Content-Type: application/json

Body:
```
{"name":{"firstName":"Google12","lastName":"Com1"},"email":"aaa2.bbb2@google.com","password":"password","ssn":"9999999999","phoneNumber":"4166666666","address":{"street1":"Yonge St","street2":"2556 unit","city":"toronto","state":"ON","zipCode":"Canada","country":"L3R 5F5"}}
```


## Link account to customer (replace the customer id and account with real Id):

POST URL: http://localhost:8083/v1/customers/toaccounts/0000015cf4b840f3-0242ac1200070000

Header: Content-Type: application/json

Body: {"id":"0000015cf4b080c7-0242ac1200070001","title":"title","owner":"google","description":"test case"}


## Transfer money from account (replace the from account and to account id with real id):

POST URL: http://localhost:8085/v1/transfers

Header: Content-Type: application/json

Body:{"fromAccountId":"2222-22222","toAccountId":"2222-22223","amount":5000,"description":"test"}


## Delete account:

DELETE URL: http://localhost:8081/v1/delete/0000015cf4bec29b-0242ac1200070001

Header: Content-Type: application/json




## Customer and Account view (use any browser)

--View the new customer by email (provide wildcard search):

  http://localhost:8084/v1/customers/aaa1.bbb1@google.com



--View the new customer by customer Id (replace the customer id with real customer id,. You can use copy from result of create customer)

  http://localhost:8084/v1/customer/{customerId}



-- view account by Id (replace the Id with real account Id)

http://localhost:8082/v1/accounts/{accountId}



-- View account transaction history:

http://localhost:8082/v1/accounts/0000015cf910e31b-0242ac1200080000/history



## End