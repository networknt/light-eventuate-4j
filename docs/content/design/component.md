---
date: 2017-06-09T13:15:31-04:00
title: Components
---

# light-eventuate-4 components:

eventuate-cdccore :                    Provide API for publishing / subscribing  events from Kafka

eventuate-cdcservice:                  Handle MySQL replication stream and publishes them to Apache Kafka;

eventuate-client/eventuate-common :    Eventuate domain and core interface

eventuate-event:                       Eventuate event handle interface and implementation

eventuate-jdbc:                        Eventuate database API for persisting event to database tables

command:                               Command side service which use to work as container to  run command side hybrid services.
                                       Copy the command side services jar files in to the /service folder in this module. And usually, we can put cdc service in the /service folder.
                                       This module is generated based on light-codegen

query:                                 Query side service which use to work as container to  run Query side hybrid  services.
                                       Copy the query side services jar files in to the /service folder in this module; This module is generated based on light-codegen


