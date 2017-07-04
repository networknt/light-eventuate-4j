---
date: 2016-10-22T20:22:34-04:00
title: Account Money Transfer
---




# Integration Test Setting


## Build related projects:

Checkout related projects.

cd ~/networknt

git clone git@github.com:networknt/light-4j.git

git clone git@github.com:networknt/light-rest-4j.git

git clone git@github.com:networknt/light-codegen.git

git clone git@github.com:networknt/light-eventuate-4j.git




## Prepare workspace
Go into the projects folder above, and build the project with maven


mvn clean install



## Build Account-management example

Get the example project from github:
git clone git@github.com:networknt/light-eventuate-example.git

cd ~/networknt/light-eventuate-example/account-management

mvn clean install


## Run the Event store and Mocroservices:


![setting](/images/setting.png)




# Test and verify result:



## From command line:

## Create new customer (C1):

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

curl -X POST \
  http://localhost:8081/v1/openaccount \
  -H 'cache-control: no-cache' \
  -H 'content-type: application/json' \
  -d '{"customerId":"0000015cf50351d8-0242ac1200060000","title":"RRSP account","description":"account for testing","initialBalance":12355}'

Result:

{"accountId":"0000015cf505ed48-0242ac1200090001","balance":12355}



## Create an account (no link with customer)

curl -X POST \\
  http://localhost:8081/v1/openaccount \
  -H 'cache-control: no-cache' \
  -H 'content-type: application/json' \
  -d '{"title":"RRSP account","description":"account for testing","initialBalance":12355}'

Result:

{"accountId":"0000015cf5084627-0242ac1200090001","balance":12355}


## Create new customer (C2):
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

curl -X POST \
  http://localhost:8083/v1/customers/toaccounts/{customerId} \
  -H 'cache-control: no-cache' \
  -H 'content-type: application/json' \
  -d '{"id":"0000015cf5084627-0242ac1200090001","title":"title","owner":"google","description":"test case"}'


Result: 0000015cf50bfe50-0242ac1200060001


## Transfer money from account (replace the from account and to account id with real id):

curl -X POST \
  http://localhost:8085/v1/transfers \
  -H 'cache-control: no-cache' \
  -H 'content-type: application/json' \
  -d '{"fromAccountId":"0000015cf505ed48-0242ac1200090001","toAccountId":"0000015cf5084627-0242ac1200090001","amount":5000,"description":"test"}'


Result:

{"moneyTransferId":"0000015cf5118e64-0242ac1200080000"}



## Delete account:

curl -X DELETE \
  http://localhost:8081/v1/delete/0000015cf4bec29b-0242ac1200070001 \
  -H 'cache-control: no-cache' \
  -H 'content-type: application/json' \




## Customer and Account view:

--View the new customer by email (provide wildcard search):

  http://localhost:8084/v1/customers/aaa1.bbb1@google.com

Result:

```
{"customers":[{"id":"0000015cf50351d8-0242ac1200060000","name":{"firstName":"Google22","lastName":"Com"},"email":"aaa1.bbb@google.com","password":"password","ssn":"9999999999","phoneNumber":"4166666666","address":{"street1":"Yonge St","street2":"2556 unit","city":"toronto","state":"ON","zipCode":"Canada","country":"L3R 5F5"},"toAccounts":null}]}
```


--View the new customer by customer Id (replace the customer id with real customer id,. You can use copy from result of create customer)

  http://localhost:8084/v1/customer/{customerId}

Result:

```
{"id":"0000015cf50351d8-0242ac1200060000","name":{"firstName":"Google22","lastName":"Com"},"email":"aaa1.bbb@google.com","password":"password","ssn":"9999999999","phoneNumber":"4166666666","address":{"street1":"Yonge St","street2":"2556 unit","city":"toronto","state":"ON","zipCode":"Canada","country":"L3R 5F5"},"toAccounts":null}
```

-- view account by Id (replace the Id with real account Id)

http://localhost:8082/v1/accounts/{accountId}

Result:

{"accountId":"0000015cf505ed48-0242ac1200090001","balance":73550000,"title":"RRSP account","description":"account for testing"}


-- View account transaction history:

http://localhost:8082/v1/accounts/0000015cf910e31b-0242ac1200080000/history

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