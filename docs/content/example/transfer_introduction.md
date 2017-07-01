---
date: 2017-06-30T20:22:34-04:00
title: Account Money Transfer Introduction
---

# Introduction

This example can be found at [https://github.com/networknt/light-eventuate-example/account-management](https://github.com/networknt/light-eventuate-example/tree/master/account-management/account-service)

Account Money Transfer example is build on light-4j, light-rest-4j and light-eventuate-4j which 
uses event sourcing and CQRS as major patterns to handle event process in multiple microservices. 

The application consists of loosely coupled components that communicate using events and leverages
eventual consistency, event-driven approach rather than using tranditional distributed transaction.

These components can be deployed either as separate services or packaged as a monolithic application 
for simplified development and testing.


# Structure of the example

Modules:

* common - common module for the application, include DDD models and events for event sourcing. 

* command - command side common components, include command, services

* query - query side common components, include command, services

* e2etest - end to end test module



## There are the following services:

* Customers Service - REST API for creating customers

* Accounts Service - REST API for creating accounts

* Transactions Service - REST API for transferring money

* Customers View Service - subscribes to events and updates query side material view. And it provides an API for retrieving customers

* Accounts View Service - subscribes to events and updates query side material view. And it provides an API for retrieving accounts



# Event work flow:

Customer/Account creation and deletion

![account1](/images/account1.png)

Money Transfer:

![account2](/images/account2.png)

