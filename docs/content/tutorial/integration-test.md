---
date: 2016-10-12T17:06:30-04:00
title: Integration Test
---

## Integration Test

1. Following the steps on "How to build, setting local and docker environment for light-eventuate-4j framework"
  To start event store, cdc service, and command/query side service

2. Send request from command side the publish events:

 From postmand, send post request:
   URL: http://localhost:8083/v1/todos
   Headers:[{"key":"Content-Type","value":"application/json","description":""}]
   Body: {"title":" this is the test todo from postman1","completed":false,"order":0}

   Response:
{
  "done": true,
  "cancelled": false,
  "completedExceptionally": false,
  "numberOfDependents": 0
}

 This request will send request which will call backe-end service to generate a "create todo" event and publish to event store.
 Event sourcing system will save the event into event store
 CDC service will be triggered and will publish event to Kafka:

3. Subscrible the event and process event on the query side:

Event sourcing system will subscrible the events from event store and process user defined event handlers.
For todo-list example, the event handle simply get the event and save the latest todo info into local TODO table.

From Postman or from brower, send GET request:

http://localhost:8082/v1/todos

Reponse:
[
  {
    "0000015c8a1b67af-0242ac1200060000": {
      "title": " this is the test todo from postman1",
      "completed": false,
      "order": 0
    }
  }
]