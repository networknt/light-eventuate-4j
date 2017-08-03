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

This tutorial is for developers who are interested in building services that leverage
light-eventuate-4j framework for service to service communication. If you want to work
on the components in light-eventuate-4j, please follow this [tutorial](https://networknt.github.io/light-eventuate-4j/tutorial/eventuate-dev/) 
to set up your working environment. 

Also, the following steps assume that you have docker installed on your computer. Your
OS should be Linux or Mac. Windows these days are still not Docker friendly and there
are some issue with docker-compose. 

## Setting DOCKER_HOST_IP for Mac

You can install Mysql, Kafka individually and start them at OS level and it is
the only option if you are using Windows. However, the most convenient way is to
use docker-compose to run the application services and eventuate infrastructure 
services: Mysql, Zookeeper, Kafka and CDC server.

There is no special configuration for Linux as Docker is native. On Mac, Docker
still runs within a VM so you need to setup OS environment variable DOCKER_HOST_IP.

This variable sets the advertised listener of the Kafka container. It must be an 
IP address (or a DNS name) that is accessible from both Docker containers and, if 
you want to do development, from applications running on the host. Unfortunately, 
because of version/platform-specific variations in how Docker works, setting this 
variable is a little tricky.

Docker for Mac has [networking limitations](https://docs.docker.com/docker-for-mac/networking/)
and you need to follow the steps below to set it up.

```
sudo ifconfig lo0 alias 10.200.10.1/24  # (where 10.200.10.1 is some unused IP address)
export DOCKER_HOST_IP=10.200.10.1
```

Once you have complete the export command, please ues the same terminal to start
docker-compose-eventuate.yml described in the next step as other terminal doesn't
have this DOCKER_HOST_IP set.


## Start Mysql, Zookeeper and Kafka

Assume you have a working directory under your home directory called networknt


```
cd ~/networknt
git clone git@github.com:networknt/light-docker.git
cd light-docker
docker-compose -f docker-compose-eventuate.yml down
docker-compose -f docker-compose-eventuate.yml up
``` 

In case the last time docker-compose is not shutdown cleanly, we run 
docker-compose down first before up. 

## Start CDC server

Open another terminal

```
cd ~/networknt/light-docker
docker-compose -f docker-compose-cdcserver.yml up
```

## Start Hybrid Command and Query servers

If you are using light-hybrid-4j to develop microservices, you don't need to start CDC server
in the above step. There is a cdc-service available and it can be started with other command
side services and share the same hybrid command server.

Here is the steps to start both command server and query server.

```
cd ~/networknt/light-docker
docker-compose -f docker-compose-hybrid.yml up
```

Once you have your dev environment setup, you can follow the [todo-list](https://networknt.github.io/light-eventuate-4j/tutorial/todo-list/) 
tutorial to get started. 
