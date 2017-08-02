---
date: 2017-08-02T09:19:29-04:00
title: Debugging Kafka
---

As we are using Kafka as message broker, we need to make sure we can debug and
monitor Kafka cluster during runtime. There are standard tools provided by Kafka
to check the topics and check the messages in each topic. These scripts are in
bin directory from Kafka installation directory. You can find a lot of online
material on how to use these scripts.

When we dockerize Kafka and run Kafka in a docker compose with zookeeper, things
will be a little different. In this tutorial, we are going to describe how to
use these scripts in docker.

The assumption is that you start the light-eventuate-4j from [light-docker](git@github.com:networknt/light-docker.git)

```
cd ~/networknt/light-docker
docker-compose -f docker-compose-eventuate.yml up
```

The above docker-compose command will start Mysql, Zookeeper and Kafka all
together. Once the compose is up, use the following docker command to log in
to Kafka container.


```
docker exec -it lightdocker_kafka_1 bash
```

Now you are in Kafka docker container.

The following command will list all the topics in Kafka.

```
bin/kafka-topics.sh --list --zookeeper zookeeper:2181
```

If you want to see the messages in one of the topics listed above, use this command

```
bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic com.networknt.eventuate.todolist.domain.TodoAggregate --from-beginning

```
