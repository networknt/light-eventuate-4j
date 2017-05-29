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

--- postgresql (if user select postgre as database to save event/entity/snapshot records)
  --TODO





## Prepare and start event sourcing command side service.

The light event sourcing framework provide a server side command service to run CDC(capture data change) and work as service container to run user defined event sourcing command side microservices

 -- copy the cdc service jar file (/cdcservice/target/eventuate-cdcservice-1.3.0.jar) to command service container folder: /light-eventuate-4j/command/service
 -- copy user defined command side service jar into same folder: /light-eventuate-4j/command/service

Run command side service:

```
java -cp ./service/*:target/eventuate-command-1.3.0.jar com.networknt.server.Server
```



## Prepare and start event sourcing query side service.

The light event sourcing framework provide a server side query service to  work as service container to run user defined event sourcing query side microservices

-- copy user defined query side service jar into same folder: /light-eventuate-4j/query/service


Run query side service

```
java -cp ./service/*:target/eventuate-query-1.3.0.jar com.networknt.server.Server
```

##  Test user event sourcing services

User can use Postman or develop UI or test class to test the services based on the swaggen


## Dockerization

Docker can simplify the application delivery. For light event sourcing system, to start event store by dockerization:
  -- go to project root folder: /light-eventuate-4j
  -- run docker-compose up
    system will start ALL required event store components (mysql, zookeeper, kafka)

 -- Same as local environment setup, copy the cdc service and user defined command side service to  /light-eventuate-4j/command/service
    copy user defined query side services to /light-eventuate-4j/query/service.

 --- go to docker module in the light-eventute-4j project: cd /light-eventuate-4j/docker
 --- run docker-compose up to start the command and query side services


## Integration

## Performance

## Production

## Conclusion

