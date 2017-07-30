---
date: 2017-07-29T22:02:00-04:00
title: Workspace setup for developing light-eventuate-4j services
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

