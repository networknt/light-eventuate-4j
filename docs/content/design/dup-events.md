---
date: 2017-06-19T08:18:08-04:00
title: How to handle duplicated events in Kafka
---

As we are using Kafka as our messaging broker for event sourcing and Kafka
guarantees messages are delivered to consumers at least once. that means we
will have duplicated events that have to be handled gracefully. 

There are several ways to handle duplicate messages. 


## Idempotency

## Vector clock

 