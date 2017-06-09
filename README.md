An eventual consistency framework implementation based on event sourcing, CQRS and Kafka on top of light-4j. 

[Developer Chat](https://gitter.im/networknt/light-eventuate-4j) |
[Documentation](https://networknt.github.io/light-eventuate-4j) |
[Contribution Guide](CONTRIBUTING.md) |

[![Build Status](https://travis-ci.org/networknt/light-eventuate-4j.svg?branch=master)](https://travis-ci.org/networknt/light-eventuate-4j)


When building microservices, there are two major patterns are used for service
to service communication.

## Synchronous, Request/Response Communication

In light-4j, this means to use Client module to call other services in the request/response
fashion regardless light-rest-4j, light-graphql-4j or light-hybrid-4j is used. 
 
## Asynchronous, Message-Based Communication

The eventual consistency framework light-eventuate-4j is designed to facilitate asynchronous
communication between services built on top of light-rest-4j, light-grahpql-4j and light-hybrid-4j.

Service communication is through events and every service maintain its own aggregates to serve consumer
independently. 

## Which Style to Choose

For pros and cons of each communication patterns, please see [here](https://networknt.github.io/light-eventuate-4j/architecture/comm-pattern/)


## Why light-eventuate-4j 

For microservices implementation, developing business transactions that update entities that are 
owned by multiple services is a challenge, as is implementing queries that retrieve data from multiple 
services; For complicated application that has too many services interact with each other. It is
essential to choose asynchronous message-based style for service communication and transaction
management with eventual consistency. 

Once you've decided that asynchronous message-based communication is best for your use case, you
need to make some architecture decision to choose the best of combination of frameworks to build
your microservices.

Light-eventuate-4j is the best framework to enable messaging-based service communication. It is
based on event sourcing and CQRS on top of Kafka message broker. You can build your services
with light-rest-4j, light-graphql-4j or light-hybrid-4j and they are all seemlessly integrated
with light-eventuate-4j framework which provide event store and event producing and consuming
client for services built on top of other light-4j frameworks.


## light-eventuate-4j components:

- light-4j, light-rest-4j, light-graphql-4j and light-hybrid-4j provide platforms to build microservices in REST, Graphql and Hybrid/RPC style.

- Mysql, Postgresql or Oracle for event store to persist events

- Zookeeper is used to manage Kafka cluster

- Apache Kafka is used as message brokers for publishing/subscribing events

- Debezium is used to publish database transaction log to Kafka

- A console/admin UI to monitor the message broker and communication between services

- A hybrid command server to host all hybrid command services in case light-hybrid-4j is used

- A hybrid query server to host all hybrid query services in case light-hybrid-4j is used
 
- A RESTful CDC service that is responsible to publish events to Kafka from event store if light-rest-4j is used.
  

