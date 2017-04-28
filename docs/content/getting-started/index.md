---
date: 2017-04-27T09:35:07-04:00
title: Getting Started
---

## Light Eventuate Framework

### Distributed data management problems in a microservice architecture:

In microservice system, normally each service has its own database. Some business transactions, 
however, span multiple services so you need a mechanism to ensure data consistency across 
services. For example, let’s imagine that you are building an e-commerce store where customers 
have a credit limit. The application must ensure that a new order will not exceed the customer’s 
credit limit. Since Orders and Customers are in different databases the application cannot simply 
use a local ACID transaction.

## Solution

A good solution to this problem is to use event sourcing. Event sourcing persists the state of 
a business entity as a sequence of state-changing events. Whenever the state of a business entity 
changes, a new event is appended to the list of events. Since saving an event is a single 
operation, it is inherently atomic. The application reconstructs an entity’s current state by 
replaying the events.

Applications save events in an event store, which is a database of events. The store has an API 
for adding and retrieving an entity’s events. The event store also behaves like a message broker. 
It provides an API that enables services to subscribe to events. When a service saves an event 
in the event store, it is delivered to all interested subscribers.
