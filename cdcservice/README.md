# Light eventuate CDC (capture data change) service

This is a service for cdc on light-eventuate-4j. By starting this service will enable cdc service for event store between mysql (or postgresql) and kafka

## Start server

If you only have one service jar file, then your can include the jar file into the
class path as below.

```
java -cp target/eventuate-command-1.3.1.jar com.networknt.server.Server
```

or

mvn exec:exec



## Docker

run "docker-compose -f docker-compose-cdcservice.yml up" from light-eventuate-4j root folder
