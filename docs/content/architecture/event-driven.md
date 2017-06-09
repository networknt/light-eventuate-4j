---
date: 2017-06-09T16:32:37-04:00
title: Event Driven Architecture
---

# Event-driven architecture

An Eventuate application consists of four types of modules, each with different roles and responsibilities:

* Command-side module
* Query-side view updater module
* Query-side view query module module
* Outbound gateway module

Note that this is the logical architecture of the application. The modules can either be deployed together as a monolithic application or separately as standalone microservices.

Command-side modules

The command-side of an Eventuate application consists of one or more command-side modules. A command-side module creates and updates aggregates in response to external update requests (for example, HTTP POST, PUT, and DELETE requests) and events published by command-side aggregates. A command-side module consists of following components :
* Aggregates - Implement most of the business logic and are persisted using event sourcing
* Services - Process external requests by creating and updating aggregates
* Event handlers - Process events by creating and updating aggregates

Query-side modules

The query-side of an Eventuate application maintains one or more materialized (CQRS) views of command-side aggregates. Each view has two modules:
* View updater module - Subscribes to events and updates the view
* View query module - Handles query (for example, HTTP GET) requests by querying the view

Outbound gateway modules

An outbound gateway module processes events by invoking external services.

Diagram:

![drawing1](/images/Drawing1.png)


Light java based Light-eventuate architecture

 In an event-driven architecture, a service publishes events when something notable happens, such as when it updates a business object. Other services subscribe to those events. In response to an event a service typically updates its own state. It might also publish more events, which then get consumed by other services

![drawing3](/images/Drawing3.png)


