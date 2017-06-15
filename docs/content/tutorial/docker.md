---
date: 2016-10-12T17:21:40-04:00
title: Docker Images for Event Store
---

## Event Store docker images

Light-eventuate-4j include event store docker files for local environment:

  /light=eventuate-4j/docker/dockerimages

     --- kafka
     --- zookeeper
     --- mysql



## build docker images

Kafka:

go to  /light=eventuate-4j/docker/dockerimages/kafka

run command:

   -- ./build.sh

Zookeeper:

go to  /light=eventuate-4j/docker/dockerimages/zookeeper

run command:

   -- ./build.sh


Mysql:

go to  /light=eventuate-4j/docker/dockerimages/mysql

run command:

   -- ./build.sh


## tag and publish docker images

   The notation for associating a local image with a repository on a registry is username/repository:tag.
   The tag is optional, but recommended, since it is the mechanism that registries use to give Docker images a version.

   docker tag image username/repository:tag

   For example:

   docker tag lighteventuate-local-kafka test/kafka:1.0.1


  Upload your tagged image to the repository:

   docker push username/repository:tag
   Once complete, the results of this upload are publicly available. If you log in to Docker Hub, you will see the new image there, with its pull command.