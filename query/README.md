# Light Hybrid 4J Query Server

This is a light-hybrid-4j server for query side services. All readonly light-hybrid-4j 
services should be deployed on this server. 

## Start server

If you only have one service jar file, then your can include the jar file into the
class path as below.

```
java -cp ../cdcservice/target/eventuate-cdcservice-1.3.0.jar:target/eventuate-query-1.3.0.jar com.networknt.server.Server
```

If you have multiple service jar files, you'd better create a directory and include
that directory into the classpath when starting the server.

```
java -cp target/eventuate-query-1.3.0.jar:/service com.networknt.server.Server
```

## Test


## Docker
