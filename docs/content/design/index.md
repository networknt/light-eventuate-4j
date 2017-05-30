---
date: 2017-04-27T09:37:10-04:00
title: Desgin
---

#Light-eventuate conceptual model

![design](/images/design.png)

Entity

An entity is a business object such as an Order.
An entity is an instance of an EntityType. In Domain-Driven Design terms, an entity is an aggregate. Each entity has an ID, unique among instances of the same entity type, which you can choose or allow Eventuate to assign to you.
An entity has a sequence of one or more events.
You can perform the following operations on an entity
•	Create - Create a new instance of the specified entity type with an initial sequence of events. Or, you can specify a unique ID.
•	Update - Append one or more events to the specified entity. Concurrent updates are handled using optimistic locking.
•	Find - Retrieve the events for the specified entity.

Entity type

An Entity type is the type of an entity. It is a String and, typically, is a fully qualified class name.

Event

An event is an object representing the occurrence of something important.
Usually an event represents the creation or update of a business object such as an OrderCreated event or an OrderCancelled event. An event can also represent the attempted violation of a business rule such a CreditCheckFailed event.
The key attributes of an event are:
•	ID - A unique indicator assigned by the system to every event. The string representation of an event ID is “x-y” where x and y are long values. Event IDs increase monotonically for a given entity.
•	Type - An event is an instance of an EventType.
•	Sender - Together, the ID and type of the business object that emitted the event.
•	*Data** - The serialization of the event’s data. The data is currently JSON.

Event Type

An EventType is the type of event objects. It is a String that is typically the fully qualified event class name.
Subscription
A Subscription is a named, durable subscription to a set of event types. The key attributes of a Subscription include:
•	ID - A unique identifier chosen by the application
•	Events - A non-empty map from EntityType to a non-empty set of EventTypes

Subscriber

A Subscriber is the client of the Eventuate Server that consumes events.
The subscriber uses the server’s STOMP API to consume events that match the specified subscription. A subscriber can consume multiple subscriptions simultaneously and multiple subscribers can consume the same subscription simultaneously.
The event delivery semantics are as follows:
•	Subscriptions are durable, so events are queued until they can be delivered to a subscriber.
•	Events published by each entity are delivered to subscribers in order.
•	Entities are partitioned across multiple concurrent subscribers for the same subscription.
•	Events are guaranteed to be delivered at least once.
•	The delivery order of events published by different aggregates is not guaranteed, that is, an OrderCreated event (for customer) might appear before a CustomerCreated event.



To use the eventuate framework in light java microservice platforms:

Developing command-side modules

A command-side module processes update requests such as HTTP POSTs, PUTs, and DELETEs. It consists of the following components:
•	Aggregates and their associated commands
•	Services, which create and update aggregates
•	Event handlers, which subscribe to events and respond by creating and updating aggregates
•	Snapshot strategies that create snapshots, which optimize the loading of aggregates

Defining commands

Defining aggregates

Writing services
Services are responsible for handling each external request (for example, an HTTP request) by either creating a new aggregate or updating an existing one. Services are typically invoked by presentation layer components such as Spring MVC controllers or by integration frameworks such as Spring Integration.

Defining event handlers
A command-side event handler subscribes to one or more event types. It processes each event by updating an existing aggregate or creating a new one.

Defining snapshot strategies
A snapshot strategy is given the opportunity to create a snapshot when an aggregate is updated.

Developing query-side modules
A query-side module maintains a materialized view of command-side aggregates. It has one or more event handlers that subscribe to events published by command-side aggregates. The event handlers process each event by updating the view.




