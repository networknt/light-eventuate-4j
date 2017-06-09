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

For pros and cons of each communication patterns, please see [here]()

## light-eventuate-4j components:

light-4j, light-rest-4j, light-graphql-4j and light-hybrid-4j provide platforms to build microservices in REST, Graphql and Hybrid/RPC style.

Mysql, Postgresql or Oracle for event store to persist events

Zookeeper is used to manage Kafka cluster

Apache Kafka is used as message brokers for publishing/subscribing events

Debezium is used to publish database transaction log to Kafka



light-eventuate-4j build on light-4j and will be use for distributed data management. [Light-4j](https://github.com/networknt/light-4j) 
is microservice platform framework; For microservice implementation, developing business 
transactions that update entities that are owned by multiple services is a challenge, as is 
implementing queries that retrieve data from multiple services;
So we build light-eventuate-4j on top of Light 4J to use Event Sourcing to handle the 
distributed data management.

Event sourcing is an event-centric approach to persistence; A service that uses event 
sourcing persists each aggregate as a sequence of events. When it creates or updates an 
aggregate, the service saves one or more events in the database, which is also known as 
the event store. It reconstructs the current state of an aggregate by loading the events 
and replaying them. In functional programming terms, a service reconstructs the state of 
an aggregate by performing a functional fold/reduce over the events. Because the events 
are the state, you no longer have the problem of atomically updating state and publishing 
events.

For the service need to use the event sourcing, the service should include light-eventuate-4j 
and implement it owen event handle. The implemented event handle will  call the API in 
light-eventuate-4j pt process/subscriber the events.



