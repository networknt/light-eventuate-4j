---
date: 2017-07-29T22:01:45-04:00
title: Workspace setup for developing light-eventuate-4j component
---

## Introduction

Microservices enable businesses to innovate faster and stay ahead of the competitions.
But one major challenge with the microservices architecture is the management of
distributed data. Each microservice has its own private database. It is difficult to
implement business transactions that maintain data consistency across multiple services
as well as queries that retrieve data from multiple services.

The light-eventuate-4j is an event sourcing and CQRS framework provides a simple yet
powerful event-driven programming model that solves the distributed data management
problems inherent in a microservice architecture.

The programming model is based on two well established patterns: Event Sourcing (ES)
and Command Query Responsibility Segregation (CQRS).

light-eventuate-4j consists of a scalable, distributed event store server and client
libraries for frameworks like light-rest-4j, light-graphql-4j and light-hybrid-4.

The following assume you are going to update or add new feature to the light-eventuate-4j
framework and install every component manually so that you have better control in order
to debug into each of them.

## Prepare Workspace

All specifications and code of the services are on github.com but we are going to
redo it again by following the steps in the tutorial. Let's first create a workspace.

I have created a directory named networknt under user directory. So my workspace is
~/networknt


## Checkout and build related projects.

```
cd ~/networknt
git clone git@github.com:networknt/light-4j.git
git clone git@github.com:networknt/light-rest-4j.git
git clone git@github.com:networknt/light-hybrid-4j.git
git clone git@github.com:networknt/light-codegen.git
git clone git@github.com:networknt/light-eventuate-4j.git
git clone git@github.com:networknt/light-eventuate-example.git

```
In fact, only light-eventuate-4j is needed; however, other projects provides source code
so that you can read and understand how services built on top of each frameworks will be
interacting with light-eventuate-4j.

Go into the project folders above one by one, and build the each project with maven

```
mvn clean install

```

## Install and start Kafka, Zookeeper and Mysql without Docker

For most people, you don't need to manually install Kafka, Zookeeper and Mysql on your
computer. It is easy to use docker-compose from [light-docker](https://github.com/networknt/light-docker)

The following steps are for two types of developers:

- DevOps guy that need to install above software package into official environments
including prod.

- Hardcore developer that don't want to install Docker and want fully control of each
component so that they can debug into them with configuration changes at ease.


The light-eventuate-4j event sourcing/CQRS framework build on event store which used to save
and dispatch events for subscribing microservices.

The event store is build upon the database Mysql (Postgres and Oracle in the future) and
distributed stream system (Kafka). Zookeeper is require to run Kafka cluster. For the local
environment without Docker, please follow below step to setup the infrastructure components.


### Install Mysql

-- Mysql (by default: we use mysql as database to save event/entity/snapshot records):

-- Download mysql database (version: 5.7.18 and above) and install it in local environment.

-- start mysql database: >mysqld

-- get and run DB setup script from folder: /light-eventuate-4j/eventuate-client/src/main/resources/mysql-event-store-schema.sql


### Install Kafka

--Download Kafka (distributed steam system) from for version (2.11-0.10.2.0 or above):
  http://www.us.apache.org/dist/kafka/0.10.2.0/kafka_2.11-0.10.2.0.tgz

-- Start zookeeper server and Kafka server:
   Kafka uses ZooKeeper so you need to first start a ZooKeeper server if you don't already have one. You can use the convenience script packaged with kafka to get a quick-and-dirty single-node ZooKeeper instance.
  > bin/zookeeper-server-start.sh config/zookeeper.properties

--- Start Kafka server
  >bin/kafka-server-start.sh config/server.properties


## Install and start Kafka, Zookeeper and Mysql with Docker

If you want to develop light-eventuate-4j and don't want to install above software package
one by one, you can install and start them in one docker-compose command.

First you need to checkout light-docker which contains all docker related files and run the
command. Make sure you have Docker installed before running docker-compose.

```$xslt
git clone git@github.com:networknt/light-docker.git
cd ~/networknt/light-docker
docker-compose -f docker-compose-eventuate.yml up
```
The above docker-compose command will start Mysql, Zookeeper and Kafka all together.

When you want to shutdown, you open another terminal and run.

```$xslt
cd ~/networknt/light-docker
docker-compose -f docker-compose-eventuate.yml down
```

You can kill the docker-compose from the same terminal by issuing CTRL+C but should run
the following command anyway in order to cleanup these docker containers.

```$xslt
docker-compose -f docker-compose-eventuate.yml down 
```

## Prepare and start CDC (Change Data Capture) RESTful server

CDC services use database binlog to populate data from database (mysql) to distributed 
stream system (kafka).

They are two different types of CDC service and one is a standalone Restful server
(cdc-server) and another one is a hybrid service(cdc-service).

The easiest way is to use the RESTful server as there is no extra dependencies.

You can start it manually by following these steps.

```
cd ~/networknt/light-eventuate-4j/cdc-server
java -jar target/cdc-server-1.4.4.jar
``` 

or 

```
cd ~/networknt/light-eventuate-4j/cdc-server
java -cp target/cdc-server-1.4.4.jar com.networknt.server.Server
```

or 

```$xslt
cd ~/networknt/light-eventuate-4j/cdc-server
mvn exec:exec
```

Another way to start CDC RESTful server is by using docker-compose.

```$xslt
cd ~/networknt/light-docker
docker-compose -f docker-compose-cdcserver.yml up
```

Normally, you only need to start CDC server manually when you are update CDC
components. Docker is much easier and convenient.


## Prepare and start CQRS command server with command services

light-eventuate-4j provides two hybrid servers that can be used to host all services
developed on top of light-hybrid-4j framework that communicate with light-eventuate-4j.

Also, it provides cdc-service which is a replacement for above cdc-server and it can be
hosted with command server along with other command services. In this case, you don't
need to start a separate Java instance. 

Normally application built on top of light-eventuate-4j will have at least two microservices:

- command side service

- query side service

These services can be built on top of light-rest-4j, light-graphql-4j or light-hybrid-4j.

If they are built with light-rest-4j or light-graphql-4j, they can be started as normal
standalone servers. 

However, if they are built with light-hybrid-4j, they can be started with hybrid-command
or hybrid-query server platforms. 

Here is the way to start command server with command services manually. Assuming all your
services and their dependencies are located at ~/networknt/light-eventuate-4j/docker/command/service

```$xslt
cd ~/networknt/light-eventuate-4j/hybrid-command
java -cp ~/networknt/light-eventuate-4j/docker/command/service/*:target/hybrid-command-1.4.4.jar com.networknt.server.Server
```


## Prepare and start CQRS query server with query services

If all your query side services are built on light-hybrid-4j framework, you can copy
them into ~/networknt/light-eventuate-4j/docker/query/service folder along with all
the dependencies that is not included in hybrid-query server. 

To start the query server manually.

```$xslt
cd ~/networknt/light-eventuate-4j/hybrid-query
java -cp ~/networknt/light-eventuate-4j/docker/query/service/*:target/hybrid-query-1.4.4.jar com.networknt.server.Server

```

## Start command server and query server together with Docker

You can start both command server and query server along with their services
with docker-compose.

```$xslt
cd ~/networknt/light-docker
docker-compose -f docker-compose-hybrid.yml up
```

Once you have your dev environment setup, you can follow the [todo-list](https://networknt.github.io/light-eventuate-4j/tutorial/todo-list/) 
tutorial to get started. 

