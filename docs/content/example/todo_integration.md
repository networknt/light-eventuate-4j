---
date: 2016-10-22T20:22:34-04:00
title: TodoList
---




# Integration Test

Following the steps on tutorial to start event store and CDC service:

## Event Store docker compose:

Docker can simplify the application delivery. For light event sourcing system, to start event store by from docker:

  -- From light-eventuate-4j project root folder: /light-eventuate-4j

  -- run docker-compose up

 system will start ALL required event store components (mysql, zookeeper, kafka)


  Prepare query side DB script: (DB script saved at: /light-eventuate-example/mysql; root user for mysql: root/rootpassword)

      create database todo_db;

      CREATE  TABLE TODO (
        ID varchar(255),
        TITLE varchar(255),
        COMPLETED BOOLEAN,
        ORDER_ID INTEGER,
        ACTIVE_FLG varchar(1) DEFAULT 'Y',
        PRIMARY KEY(ID)
      );

      GRANT ALL PRIVILEGES ON todo_db.* TO 'mysqluser' IDENTIFIED BY 'mysqlpw';


##  Start CDC service:

  -- From light-eventuate-4j project root folder: /light-eventuate-4j

  -- run docker-compose -f docker-compose-cdcservice.yml up

-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------




## Build todo-list application:

From the root folder of the todo-list project: /light-eventuate-example/todo-list, use maven to build the project:

```
cmd: mvn clean install
```



## Dockerization

  -- go to project root folder: /light-eventuate-example/todo-list/

  run command:

  docker-compose up





## Local environment test

Go to the  command service folder:/light-eventuate-example/todo-list/command-service:

Start command side service:
```
mvn exec:exec
```

Or
```
java -jar ./target/eventuate-todo-command-service-0.1.0.jar
```



For query side service:
```

Go to the  query service folder:/light-eventuate-example/todo-list/query-service::

Start command side service:
```
mvn exec:exec
```

Or
```
java -jar ./target/eventuate-todo-query-service-0.1.0.jar
```





## Process Flow

![drawing5](/images/Drawing5.png)

Step 1:  Application or browser send restful http post request to command service to create a todo :

Step 2: Command service process the request and generate a "create todo" event; And the publish the event to event store

Step 3: CDC service will capture the data change based on mysql/postgres replication log and publish the event data to Kafka distributed stream system

Step 4: Query side service's event handler will know the new event data published to Kafka, and event handler will subscribe the event

Step 5: Query side service will process the event and Save the result to local table. (In this example: mysql/todo_db/TODO)

Step 6: Application or browser send restful http get request to query service to get ALL todo list

Step 7:  Query side service  process the request and get the todo list from local table

Step 8: Query side service return the http response back.


##Verify result:

1. Send request from command side the publish events:

 From postmand, send post request:

   URL: http://localhost:8083/v1/todos;

   Headers:[{"key":"Content-Type","value":"application/json","description":""}];

   Body: {"title":" this is the test todo from postman1","completed":false,"order":0};

   Response:
{
  "done": true,
  "cancelled": false,
  "completedExceptionally": false,
  "numberOfDependents": 0
};

 This request will send request which will call backe-end service to generate a "create todo" event and publish to event store.

 Event sourcing system will save the event into event store.

 CDC service will be triggered and will publish event to Kafka:

The request will publish a "CreateTodo" event and will save the entity/event to the event store mysql database.

 we can use sql to verify:

 select * from entity;

 select * from events;




2. Subscrible the event and process event on the query side:

Event sourcing system will subscrible the events from event store and process user defined event handlers.
For todo-list example, the event handle simply get the event and save the latest todo info into local TODO table.

From Postman or from brower, send GET request:

http://localhost:8082/v1/todos;

Reponse:
[
  {
    "0000015c8a1b67af-0242ac1200060000": {
      "title": " this is the test todo from postman1",
      "completed": false,
      "order": 0
    }
  }
];


## End