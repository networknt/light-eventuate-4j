---
date: 2017-06-09T18:20:57-04:00
title: Event Sourcing
---


Event sourcing is an event-centric approach to persistence; A service that uses event 
sourcing persists each aggregate as a sequence of events. When it creates or updates an 
aggregate, the service saves one or more events in the database, which is also known as 
the event store. It reconstructs the current state of an aggregate by loading the events 
and replaying them. In functional programming terms, a service reconstructs the state of 
an aggregate by performing a functional fold/reduce over the events. Because the events 
are the state, you no longer have the problem of atomically updating state and publishing 
events.

For the service need to use the event sourcing, the service should include light-eventuate-4j 
and implement it owen event handle. The implemented event handle will call the API in 
light-eventuate-4j to process/subscriber the events.



