# Light Hybrid 4J Query Server

This is a light-hybrid-4j server for query side services. All readonly light-hybrid-4j
services should be deployed on this server.

## Start server

If you only have one service jar file, then your can include the jar file into the
class path as below.

```
java -cp ../cdcservice/target/eventuate-cdcservice-1.4.6.jar:target/eventuate-query-1.4.6.jar com.networknt.server.Server
```

for windows:

```
java -cp ../cdcservice/target/eventuate-cdcservice-1.4.6.jar;target/eventuate-query-1.4.6.jar com.networknt.server.Server
```

If you have multiple service jar files, you'd better create a directory and include
that directory into the classpath when starting the server.

```
java -cp ./service/*:target/eventuate-query-1.4.6.jar com.networknt.server.Server
```

for windows:

```
java -cp ./service/*;target/eventuate-query-1.4.6.jar com.networknt.server.Server
```
## Test


## Docker
run docker-compose -f docker-compose-service.yml up from light-eventuate-4j root folder
  -- it will build and run the Dockerfile under command module