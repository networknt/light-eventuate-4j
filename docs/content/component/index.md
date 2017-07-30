---
date: 2017-07-29T18:51:48-04:00
title: Components
---

There are too many components in light-eventuate-4j and they can be categorized
to four groups.

## Data change capture

### cdc-common

This module contains some common classes and configuration for data change capture.

### cdc-mysql

This module contains Mysql related classes for data change capture. It is based on
[Mysql binlog connector for Java](https://github.com/shyiko/mysql-binlog-connector-java)

### cdc-server

This is a microservice implemented on top of light-rest-4j framework. It constantly
monitors Mysql binlog and pushes EVENTS table changes to Kafka topic. This server can be
started with a docker-compose file in [light-docker](https://github.com/networknt/light-docker)

Assume you are using ~/networknt as your working directory.

```
git clone git@github.com:networknt/light-docker.git
docker-compose -f docker-compose-cdcserver.yml up
```


### cdc-service

This is a microservice implemented on top of light-hybrid-4j framework. It monitors Mysql
binlog and pushes EVENTS table changes to Kafka topic. Unlike the cdc-server above, this
service cannot be started standalone. It must be embedded into hybrid-command server
described below.

Note: the entire light-eventuate-4j platform only need one Data Change Capture service and
you can choose the restful cdc-server or hybrid cdc-service but make sure that only one of
them is running.

## Event store and client

### eventuate-common

This module contains some classes that shared by all other modules. It defines interfaces
and provides implementations for most of them.

### eventuate-event

This module contains all the classes for event dispatcher and handler.

### eventuate-kafka

This module is responsible for Kafka publishing and consuming.

### eventuate-client

This is the module that user services will be using to communicate with light-eventuate-4j
platform.

### eventuate-jdbc

This module contains classes related to database communications.

### eventuate-test

A test module that test some of the features and concept for light-eventuate-4j. Some of the
tests are located in this module due to cycle dependency or the test across several modules.

### eventuate-console

This is a Javascript Single Page Application that is used to monitor the light-eventuate-4j
platform.


## Test module and utility

### test-util

This module contains some utility classes to facilitate testing.

### test-domain

Account management domain object defined for some test cases.

### test-example

Example classes that uses test-domain classes

### test-jdbc

Testing JDBC classes for db repository.

## Hybrid command and query servers

### hybrid-command

Hybrid server platform for command side services. You can have multiple command side services
that are deployed with one instance of the hybrid-command.

### hybrid-query

Hybrid server platform for query side services. You can have multiple query side services
that are deployed with one instance of the hybrid-query.

