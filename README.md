:# light-eventuate
An eventuate framework implementation based on event sourcing and CQRS on top of Light Java. 

light-eventuate build on light-4j and will be use for distributed data management. Light 4j 
is microservice platform framework; For microservice implementation, developing business 
transactions that update entities that are owned by multiple services is a challenge, as is 
implementing queries that retrieve data from multiple services;
So we build light-eventuate-4j on top of Light 4J to use Event Sourcing to handle the 
distributed data management.

Event sourcing is an event-centric approach to persistence; A service that uses event 
sourcing persists each aggregate as a sequence of events. When it creates or updates an 
aggregate, the service saves one or more events in the database, which is also known as 
the event store. It reconstructs the current state of an aggregate by loading the events 
and replaying them. In functional programming terms, a service reconstructs the state of 
an aggregate by performing a functional fold/reduce over the events. Because the events 
are the state, you no longer have the problem of atomically updating state and publishing 
events.

For the service need to use the event sourcing, the service should include light-eventuate-4j 
and implement it owen event handle. The implemented event handle will  call the API in 
light-eventuate-4j pt process/subscriber the events.


# light-eventuate components:

light-4j:      Provide microservice platform; Restful service.
mysql:         Database for persisting events information
zookeeper:     Server used for Kafka
Apache Kafka:  Message broker for publishing / subscribing  events


# light-eventuate project structure:

eventuate-cdccore :                    Provide API for publishing / subscribing  events from Kafka

eventuate-cdcservice:                   Handle MySQL replication stream and publishes them to Apache Kafka;

eventuate-client/eventuate-common :    Eventuate domain and core interface

eventuate-event:                       Eventuate event handle interface and implementation

eventuate-jdbc:                        Eventuate database API for persisting event to database tables

command:                               Command side service which use to work as container to  run command side hybrid services.
                                       Copy the command side services jar files in to the /service folder in this module. And usually, we can put cdc service in the /service folder.
                                       This module is generated based on light-codegen

query:                                 Query side service which use to work as container to  run Query side hybrid  services.
                                       Copy the query side services jar files in to the /service folder in this module; This module is generated based on light-codegen


# light-eventuate tasks TODO list:

enrich the eventuate-cdccore package by adding Kafka stream API to handle events in stream 
chain; implement Stream as Table.

enrich eventuate-jdbc to handle different database.

add more junit cases for the project.

create an admin console for project to monitor the event process and the event sync between different service.

create example project  top of Light 4J and Light Eventuate 4J.




