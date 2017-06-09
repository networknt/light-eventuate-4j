---
date: 2016-10-09T08:01:56-04:00
title: Microservices
---

## Introduction

Microservices enable businesses to innovate faster and stay ahead of the competition.
But one major challenge with the microservices architecture is the management of distributed data. Each microservice has its own private database.
It is difficult to implement business transactions that maintain data consistency across multiple services as well as queries that retrieve data from multiple services.

The light event sourcing framework provides a simple yet powerful event-driven programming model that solves the distributed data management problems inherent in a microservice architecture.
The programming model is based on two well established patterns: Event Sourcing and Command Query Responsibility Segregation (CQRS).
Eventuate consists of a scalable, distributed event store server and client libraries for various languages and frameworks.

## Prepare workspace

All specifications and code of the services are on github.com but we are going to
redo it again by following the steps in the tutorial. Let's first create a
workspace. I have created a directory named networknt under user directory.

Checkout related projects.

```
cd ~/networknt
git clone git@github.com:networknt/light-4j.git
git clone git@github.com:networknt/light-rest-4j.git
git clone git@github.com:networknt/light-codegen.git
git clone git@github.com:networknt/light-eventuate-4j.git
git clone git@github.com:networknt/light-eventuate-example.git
git clone git@github.com:networknt/light-hybrid-4j.git

## Prepare workspace

Go into the projects folder above, and build the project with maven

```
mvn clean install

```

## Prepare and start event store

The light event sourcing framework build on event store which used to save and dispatch events for calling microservices.
The event store is build upon the database (mysql or postgresql) and distributed stream system (Kafka). For the local environment (wothout docker). Please follow below step to setup event store.


--- Mysql (by default: use mysql as database to save event/entity/snapshot records):

-- Download mysql database (version: 5.7.18 and above) and install it in local environment.

-- start mysql database: >mysqld

-- get and run DB setup script from folder: /light-eventuate-4j/client/src/main/resources/mysql-event-store-schema.sql



--- Kafka:

--Download Kafka (distributed steam system) from for version (2.11-0.10.2.0 or above):
  http://www.us.apache.org/dist/kafka/0.10.2.0/kafka_2.11-0.10.2.0.tgz

-- Start zookeeper server and Kafka server:
   Kafka uses ZooKeeper so you need to first start a ZooKeeper server if you don't already have one. You can use the convenience script packaged with kafka to get a quick-and-dirty single-node ZooKeeper instance.
  > bin/zookeeper-server-start.sh config/zookeeper.properties

--- Start Kafka server
  >bin/kafka-server-start.sh config/server.properties

--- postgresql (if user select postgreSql as database to save event/entity/snapshot records)
  --TODO



## Prepare and start event sourcing CDC (capture data change service).

CDC service use database replication to populate data from database (mysql) to distributed stream system (kafka).

-- go to cdcservice project module:
  cd cdcservice

-- run command line:
  ```
  java -cp target/eventuate-command-1.3.1.jar com.networknt.server.Server
  ```

  or

  mvn exec:exec


## Prepare and start event sourcing command side service and query side service.

Normally event sourcing system will have at least two microservices:

command side service

query side service

For example, to use our simple todo-list example:


 Go to light-eventuate-example/todo-list
 Build the project from command line:
   --mvn clean install

## Command side service

 Go command-service module, run command line:
   -- mvn package exec:exec


## Query side service

 Prepare query side DB script: (DB script saved at: /light-eventuate-example/mysql)


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

 Go query-service module, run command line:
   -- mvn package exec:exec





## Dockerization


# Event Store docker compose:

Docker can simplify the application delivery. For light event sourcing system, to start event store by dockerization:

  -- From light-eventuate-4j project root folder: /light-eventuate-4j

  -- run docker-compose up

 system will start ALL required event store components (mysql, zookeeper, kafka)


  For todo-list exampleocumentdocumentdd:
   Prepare query side DB script: (DB script saved at: /light-eventuate-example/mysql)

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



# For Restful Services

 Start CDC service:

  -- From light-eventuate-4j project root folder: /light-eventuate-4j

  -- run docker-compose -f docker-compose-cdcservice.yml up

 Run Command/Query side service:


  -- From todo-list example root folder: /light-eventuate-example/todo-list

  -- run docker-compose up



# For Hybrid API Services

  -- From light-eventuate-4j project root folder: /light-eventuate-4j

  -- Run docker-compose -f docker-compose-service.yml up



## Integration

## Performance

## Production

## Conclusion

