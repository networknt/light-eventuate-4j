---
date: 2017-06-09T14:39:01-04:00
title: Communication Patterns
---

# Introduction

In a monolithic application, components invoke one another via method or function calls. In contrast,
a microservices-based application might be distributed on multiple Docker containers or VMs or multiple
machines. Each service instance is typically a process and services much interact using an inter-process
communication mechanism. 

# Patterns

There are two major styles in client/service communication. 

## Synchronous

The client expects a timely response from the service and might even block while it waits.

When using a synchronous, request/response-based IPC mechanism, a client sends a request to a service. 
The service processes the request and sends back a response. In many clients, the thread that makes the 
request blocks while waiting for a response. Other clients might use asynchronous, event-driven client 
code that is perhaps encapsulated by Futures or Rx Observables. However, unlike when using messaging, the 
client assumes that the response will arrive in a timely fashion. There are numerous styles to choose 
from. Two popular HTTP styles are REST and RPC. 

There are numerous benefits to using a communication style that is based on HTTP:

- HTTP is simple and familiar.
- You can test an HTTP API from within a browser using an extension such as Postman or from the command 
line using curl (assuming JSON or some other text format is used).
- It directly supports request/response-style communication.
- HTTP is, of course, firewall-friendly.
- It doesn’t require an intermediate broker, which simplifies the system’s architecture.

There are some drawbacks to using HTTP:

- It only directly supports the request/response style of interaction. You can use HTTP for notifications 
but the server must always send an HTTP response.
- Because the client and service communicate directly (without an intermediary to buffer messages), they 
must both be running for the duration of the exchange.
- The client must know the location (i.e., the URL) of each service instance and this is a non-trivial 
problem in a modern application. Clients must use a service discovery mechanism to locate service instances.


## Asynchronous, Message-Based Communication

The client doesn’t block while waiting for a response, and the response, if any, isn’t necessarily 
sent immediately. 

When using messaging, processes communicate by asynchronously exchanging messages. A client makes a 
request to a service by sending it a message. If the service is expected to reply, it does so by sending 
a separate message back to the client. Since the communication is asynchronous, the client does not block 
waiting for a reply. Instead, the client is written assuming that the reply will not be received immediately.

A message consists of headers (metadata such as the sender) and a message body. Messages are exchanged 
over channels. Any number of producers can send messages to a channel. Similarly, any number of consumers 
can receive messages from a channel. 

There are many advantages to using messaging:

- Decouples the client from the service

A client makes a request simply by sending a message to the appropriate channel. The client is completely 
unaware of the service instances. It does not need to use a discovery mechanism to determine the location 
of a service instance.

- Message buffering 

With a synchronous request/response protocol, such as a HTTP, both the client and service must be available 
for the duration of the exchange. In contrast, a message broker queues up the messages written to a channel 
until they can be processed by the consumer. This means, for example, that an online store can accept orders 
from customers even when the order fulfillment system is slow or unavailable. The order messages simply queue 
up.

- Flexible client-service interactions 

Messaging supports all of the interaction styles described earlier.


- Explicit inter-process communication 

RPC-based mechanisms attempt to make invoking a remote service look the same as calling a local service. 
However, because of the laws of physics and the possibility of partial failure, they are in fact quite 
different. Messaging makes these differences very explicit so developers are not lulled into a false sense 
of security.


There are, however, some downsides to using messaging:

- Additional operational complexity 

The messaging system is yet another system component that must be installed, configured, and operated. It’s 
essential that the message broker be highly available, otherwise system reliability is impacted.


- Complexity of implementing request/response-based interaction 

Request/response-style interaction requires some work to implement. Each request message must contain a reply 
channel identifier and a correlation identifier. The service writes a response message containing the 
correlation ID to the reply channel. The client uses the correlation ID to match the response with the request. 
It is often easier to use an IPC mechanism that directly supports request/response.

- Client needs to discover location of message broker

Given message broker is the most important of component in the infrastructure, it is normally a cluster of
serveral instances. This makes client more complicated to locate which instance to communicate with and how
to fail over to other instances when the current one fails. 


# Summary

Microservices must communicate using an inter-process communication mechanism. When designing how your 
services will communicate, you need to consider various issues: how services interact, how to specify the 
API for each service, how to evolve the APIs, and how to handle partial failure. There are two kinds of IPC 
mechanisms that microservices can use, asynchronous messaging and synchronous request/response. 