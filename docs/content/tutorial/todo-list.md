---
date: 2017-07-30T10:45:03-04:00
title: Todo list tutorial
---

This is the most simple Hello World type of application with event sourcing
and CQRS on top of light-eventuate-4j. It has command side service that add
new todo item, update existing todo item and remove todo item(s). It also
has query side service to retrieve all the todo items for display in the web
single page application. 

The following steps will assume you know the basic about light-eventuate-4j
as well as light-rest-4j and light-hybrid-4j as we are going to build services
in RESTful style and Hybrid style. 

Before starting any service, we need to make sure that light-eventuate-4j is
up and running. Please follow this [tutorial](https://networknt.github.io/light-eventuate-4j/tutorial/service-dev/) 
to set up.


We are going to implement the application in both light-rest-4j and light-hybrid-4
and the two implementations will be sitting in the same folders in light-example-4j. 

As the todo-list folder already in the public repo of light-example-4j/eventuate, we 
are going to rename the existing project and you can compare these folders if you want.

## Prepare environment

Assuming that light-eventuate-4j platform is up and running, let's checkout the
light-example-4j project and rename todo-list folder in eventuate so that we can
recreate it in this tutorial.

As other tutorials, we are going to use networknt under user's home directory as
workspace. 

```
cd ~/networknt
git clone git@github.com:networknt/light-example-4j.git
cd light-example-4j/eventuate
mv todo-list todo-list.bak
``` 

Once all done, the todo-list project in light-example-4j/eventuate will contain POJOs 
, two restful services and two hybrid services.  

## Create a multiple modules maven project

Let's create a todo-list folder and copy the pom.xml from the existing project.
```
cd ~/networknt/light-example-4j/eventuate
mkdir todo-list
cd todo-list
cp ../todo-list.bak/pom.xml .

```

Now let's copy common, command and query modules to the todo-list folder from existing
one.

- common contains events and model
- command contains command definitions and command side service
- query contains query side service
 

```
cd ~/networknt/light-example-4j/eventuate/todo-list
cp -r ../todo-list.bak/common .
cp -r ../todo-list.bak/command .
cp -r ../todo-list.bak/query .

```

Now let's open pom.xml to remove other modules and only leave common, command and query:

```
    <modules>
        <module>common</module>
        <module>command</module>
        <module>query</module>
    </modules>

```

Let's run the maven build to make sure these three modules can be built.

```
cd ~/networknt/light-example-4j/eventuate/todo-list
mvn clean install
```

## Command side restful service

In this step we are going to generate a command side restful microserivce. The model-config
for this service can be found at model-config repo.

Let's first checkout model-config repo from github.

```
git clone git@github.com:networknt/model-config.git

```

The swagger specification for todo-list command side service can be found at
~/networknt/model-config/rest/todo-command. In the same folder, there is config.json
which is used to control how light-codegen going to generate the code.

In order to generate the rest-command service, let's first clone the light-codegen
project and build it.

```
cd ~/networknt
git clone git@github.com:networknt/light-codegen.git
cd light-codegen
mvn clean install
```

Generate rest-command service with the following command line. Assume we are still in
light-codegen folder.

```
java -jar codegen-cli/target/codegen-cli.jar -f light-rest-4j -o ../light-example-4j/eventuate/todo-list/rest-command -m ../model-config/rest/todo_command/1.0.0/swagger.json -c ../model-config/rest/todo_command/1.0.0/config.json
``` 

Now add this rest-command into pom.xml and build it with maven.

```xml
    <modules>
        <module>common</module>
        <module>command</module>
        <module>query</module>
        <module>rest-command</module>
    </modules>

```

```
mvn clean install
```

The four modules should be built successfully.

Now let's update rest-command module to wire the logic.

First we need to update dependencies for this project by adding the following.
```xml
        <version.light-eventuate-4j>1.3.5</version.light-eventuate-4j>


        <dependency>
            <groupId>com.networknt</groupId>
            <artifactId>todo-common</artifactId>
            <version>0.1.0</version>
        </dependency>
        <dependency>
            <groupId>com.networknt</groupId>
            <artifactId>todo-command</artifactId>
            <version>0.1.0</version>
        </dependency>
        <dependency>
            <groupId>com.networknt</groupId>
            <artifactId>eventuate-common</artifactId>
            <version>${version.light-eventuate-4j}</version>
        </dependency>
        <dependency>
            <groupId>com.networknt</groupId>
            <artifactId>eventuate-jdbc</artifactId>
            <version>${version.light-eventuate-4j}</version>
        </dependency>
        <dependency>
            <groupId>com.networknt</groupId>
            <artifactId>eventuate-client</artifactId>
            <version>${version.light-eventuate-4j}</version>
        </dependency>
        <dependency>
            <groupId>com.networknt</groupId>
            <artifactId>test-jdbc</artifactId>
            <version>${version.light-eventuate-4j}</version>
        </dependency>

```

And change the three handlers to the following.

```java
package com.networknt.todolist.restcommand.handler;

import com.networknt.body.BodyHandler;
import com.networknt.config.Config;
import com.networknt.eventuate.common.AggregateRepository;
import com.networknt.eventuate.common.EventuateAggregateStore;
import com.networknt.eventuate.todolist.TodoCommandService;
import com.networknt.eventuate.todolist.TodoCommandServiceImpl;
import com.networknt.eventuate.todolist.common.model.TodoInfo;
import com.networknt.eventuate.todolist.domain.TodoAggregate;
import com.networknt.eventuate.todolist.domain.TodoBulkDeleteAggregate;
import com.networknt.service.SingletonServiceFactory;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class TodosIdDeleteHandler implements HttpHandler {

    private EventuateAggregateStore eventStore  = (EventuateAggregateStore) SingletonServiceFactory.getBean(EventuateAggregateStore.class);

    private AggregateRepository todoRepository = new AggregateRepository(TodoAggregate.class, eventStore);
    private AggregateRepository bulkDeleteAggregateRepository  = new AggregateRepository(TodoBulkDeleteAggregate.class, eventStore);

    private TodoCommandService service = new TodoCommandServiceImpl(todoRepository, bulkDeleteAggregateRepository);

    public void handleRequest(HttpServerExchange exchange) throws Exception {

        // delete a new object
        String id = exchange.getQueryParameters().get("id").getFirst();
        TodoInfo todo = (TodoInfo)exchange.getAttachment(BodyHandler.REQUEST_BODY);
        CompletableFuture<TodoInfo> result = service.remove(id).thenApply((e) -> {
            TodoInfo m = e.getAggregate().getTodo();
            return m;
        });
        exchange.getResponseHeaders().add(new HttpString("Content-Type"), "application/json");
        exchange.getResponseSender().send(Config.getInstance().getMapper().writeValueAsString(result));
    }
}

```

```java
package com.networknt.todolist.restcommand.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.body.BodyHandler;
import com.networknt.config.Config;
import com.networknt.eventuate.common.AggregateRepository;
import com.networknt.eventuate.common.EventuateAggregateStore;
import com.networknt.eventuate.todolist.TodoCommandService;
import com.networknt.eventuate.todolist.TodoCommandServiceImpl;
import com.networknt.eventuate.todolist.common.model.TodoInfo;
import com.networknt.eventuate.todolist.domain.TodoAggregate;
import com.networknt.eventuate.todolist.domain.TodoBulkDeleteAggregate;
import com.networknt.service.SingletonServiceFactory;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class TodosPostHandler implements HttpHandler {

    private EventuateAggregateStore eventStore  = (EventuateAggregateStore) SingletonServiceFactory.getBean(EventuateAggregateStore.class);

    private AggregateRepository todoRepository = new AggregateRepository(TodoAggregate.class, eventStore);
    private AggregateRepository bulkDeleteAggregateRepository  = new AggregateRepository(TodoBulkDeleteAggregate.class, eventStore);

    private TodoCommandService service = new TodoCommandServiceImpl(todoRepository, bulkDeleteAggregateRepository);


    public void handleRequest(HttpServerExchange exchange) throws Exception {
        System.out.println("command side:");
        ObjectMapper mapper = new ObjectMapper();

        // add a new object
        Map s = (Map)exchange.getAttachment(BodyHandler.REQUEST_BODY);
        String json = mapper.writeValueAsString(s);
        TodoInfo todo = mapper.readValue(json, TodoInfo.class);

        //TodoInfo todo2 = JSonMapper.fromJson(exchange.getAttachment(BodyHandler.REQUEST_BODY),  TodoInfo.class);
        CompletableFuture<TodoInfo> result = service.add(todo).thenApply((e) -> {
            TodoInfo m = e.getAggregate().getTodo();
            return m;
        });

        exchange.getResponseHeaders().add(new HttpString("Content-Type"), "application/json");
        exchange.getResponseSender().send(Config.getInstance().getMapper().writeValueAsString(result));
    }
}

```

```java
package com.networknt.todolist.restcommand.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.body.BodyHandler;
import com.networknt.config.Config;
import com.networknt.eventuate.common.AggregateRepository;
import com.networknt.eventuate.common.EventuateAggregateStore;
import com.networknt.eventuate.common.impl.sync.AggregateCrud;
import com.networknt.eventuate.todolist.TodoCommandService;
import com.networknt.eventuate.todolist.TodoCommandServiceImpl;
import com.networknt.eventuate.todolist.common.model.TodoInfo;
import com.networknt.eventuate.todolist.domain.TodoAggregate;
import com.networknt.eventuate.todolist.domain.TodoBulkDeleteAggregate;
import com.networknt.service.SingletonServiceFactory;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class TodosPutHandler implements HttpHandler {
    private AggregateCrud aggregateCrud = (AggregateCrud) SingletonServiceFactory.getBean(AggregateCrud.class);
    private EventuateAggregateStore eventStore  = (EventuateAggregateStore)SingletonServiceFactory.getBean(EventuateAggregateStore.class);
    // private TodoCommandService service = (TodoCommandService)SingletonServiceFactory.getBean(TodoCommandService.class);

    private AggregateRepository todoRepository = new AggregateRepository(TodoAggregate.class, eventStore);
    private AggregateRepository bulkDeleteAggregateRepository  = new AggregateRepository(TodoBulkDeleteAggregate.class, eventStore);

    private TodoCommandService service = new TodoCommandServiceImpl(todoRepository, bulkDeleteAggregateRepository);

    public void handleRequest(HttpServerExchange exchange) throws Exception {
        ObjectMapper mapper = new ObjectMapper();      // update a new object
        String id = exchange.getQueryParameters().get("id").getFirst();
        Map s = (Map)exchange.getAttachment(BodyHandler.REQUEST_BODY);
        String json = mapper.writeValueAsString(s);
        TodoInfo todo = mapper.readValue(json, TodoInfo.class);

        CompletableFuture<TodoInfo> result = service.update(id, todo).thenApply((e) -> {
            TodoInfo m = e.getAggregate().getTodo();
            return m;
        });
        exchange.getResponseHeaders().add(new HttpString("Content-Type"), "application/json");
        exchange.getResponseSender().send(Config.getInstance().getMapper().writeValueAsString(result));
    }
}

```

Since we are going to create some eventuate class instances from interfaces. Let's create a
service.yml under resources/config folder.

```yaml
singletons:
- javax.sql.DataSource:
  - com.zaxxer.hikari.HikariDataSource:
      DriverClassName: com.mysql.jdbc.Driver
      jdbcUrl: jdbc:mysql://localhost:3306/eventuate?useSSL=false
      username: mysqluser
      password: mysqlpw
- com.networknt.eventuate.common.impl.sync.AggregateCrud:
  - com.networknt.eventuate.client.EventuateLocalDBAggregateCrud
- com.networknt.eventuate.common.impl.sync.AggregateEvents:
    - com.networknt.eventuate.client.EventuateLocalAggregatesEvents
- com.networknt.eventuate.common.impl.AggregateCrud:
  - com.networknt.eventuate.common.impl.adapters.SyncToAsyncAggregateCrudAdapter
- com.networknt.eventuate.common.impl.AggregateEvents:
  - com.networknt.eventuate.common.impl.adapters.SyncToAsyncAggregateEventsAdapter
- com.networknt.eventuate.common.SnapshotManager:
  - com.networknt.eventuate.common.SnapshotManagerImpl
- com.networknt.eventuate.common.MissingApplyEventMethodStrategy:
  - com.networknt.eventuate.common.DefaultMissingApplyEventMethodStrategy
- com.networknt.eventuate.common.EventuateAggregateStore:
  - com.networknt.eventuate.common.impl.EventuateAggregateStoreImpl
- com.networknt.eventuate.common.sync.EventuateAggregateStore:
  - com.networknt.eventuate.common.impl.sync.EventuateAggregateStoreImpl
- com.networknt.eventuate.todolist.domain.TodoAggregate:
  - com.networknt.eventuate.todolist.domain.TodoAggregate
- com.networknt.eventuate.todolist.domain.TodoBulkDeleteAggregate:
  - com.networknt.eventuate.todolist.domain.TodoBulkDeleteAggregate
```

Now let's verify that all modules can be built.

```
mvn clean install
```


## Query side restful service

The swagger specification for todo-list query side service can be found at
~/networknt/model-config/rest/todo-query. In the same folder, there is config.json
which is used to control how light-codegen going to generate the code.

Generate rest-query service with the following command line. Assume we are still in
light-codegen folder.

```
java -jar codegen-cli/target/codegen-cli.jar -f light-rest-4j -o ../light-example-4j/eventuate/todo-list/rest-query -m ../model-config/rest/todo_query/1.0.0/swagger.json -c ../model-config/rest/todo_query/1.0.0/config.json
``` 

Now add this rest-query into parent pom.xml and build it with maven.

```xml
    <modules>
        <module>common</module>
        <module>command</module>
        <module>query</module>
        <module>rest-command</module>
        <module>rest-query</module>
    </modules>

```

```
mvn clean install
```

The five modules should be built successfully.

Now let's update rest-query module to wire the logic.

First we need to update dependencies for this project by adding the following.
```xml
        <version.light-eventuate-4j>1.3.5</version.light-eventuate-4j>


        <dependency>
            <groupId>com.networknt</groupId>
            <artifactId>todo-common</artifactId>
            <version>0.1.0</version>
        </dependency>
        <dependency>
            <groupId>com.networknt</groupId>
            <artifactId>todo-command</artifactId>
            <version>0.1.0</version>
        </dependency>
        <dependency>
            <groupId>com.networknt</groupId>
            <artifactId>todo-query</artifactId>
            <version>0.1.0</version>
        </dependency>
        <dependency>
            <groupId>com.networknt</groupId>
            <artifactId>eventuate-common</artifactId>
            <version>${version.light-eventuate-4j}</version>
        </dependency>
        <dependency>
            <groupId>com.networknt</groupId>
            <artifactId>eventuate-jdbc</artifactId>
            <version>${version.light-eventuate-4j}</version>
        </dependency>
        <dependency>
            <groupId>com.networknt</groupId>
            <artifactId>eventuate-client</artifactId>
            <version>${version.light-eventuate-4j}</version>
        </dependency>
        <dependency>
            <groupId>com.networknt</groupId>
            <artifactId>test-jdbc</artifactId>
            <version>${version.light-eventuate-4j}</version>
        </dependency>

```

Now update the two handler as following.

```java
package com.networknt.todolist.restquery.handler;

import com.networknt.config.Config;
import com.networknt.eventuate.todolist.TodoQueryService;
import com.networknt.eventuate.todolist.common.model.TodoInfo;
import com.networknt.service.SingletonServiceFactory;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TodosGetHandler implements HttpHandler {


    TodoQueryService service =
            (TodoQueryService) SingletonServiceFactory.getBean(TodoQueryService.class);

    public void handleRequest(HttpServerExchange exchange) throws Exception {

        List<Map<String, TodoInfo>> resultAll = service.getAll();
        exchange.getResponseHeaders().add(new HttpString("Content-Type"), "application/json");
        exchange.getResponseSender().send(Config.getInstance().getMapper().writeValueAsString(resultAll));
    }
}

```


```java
package com.networknt.todolist.restquery.handler;

import com.networknt.config.Config;
import com.networknt.eventuate.todolist.TodoQueryService;
import com.networknt.eventuate.todolist.common.model.TodoInfo;
import com.networknt.service.SingletonServiceFactory;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class TodosIdGetHandler implements HttpHandler {
    TodoQueryService service =
            (TodoQueryService) SingletonServiceFactory.getBean(TodoQueryService.class);

    public void handleRequest(HttpServerExchange exchange) throws Exception {
        String id = exchange.getQueryParameters().get("id").getFirst();
        CompletableFuture<Map<String, TodoInfo>> result = service.findById(id);
        exchange.getResponseHeaders().add(new HttpString("Content-Type"), "application/json");
        exchange.getResponseSender().send(Config.getInstance().getMapper().writeValueAsString(result));
    }
}

```



Since we are going to create some eventuate class instances from interfaces. Let's create a
service.yml under src/main/resources/config folder.

```yaml
singletons:
- javax.sql.DataSource:
  - com.zaxxer.hikari.HikariDataSource:
      DriverClassName: com.mysql.jdbc.Driver
      jdbcUrl: jdbc:mysql://localhost:3306/todo_db?useSSL=false
      username: mysqluser
      password: mysqlpw
- com.networknt.eventuate.common.impl.sync.AggregateCrud:
  - com.networknt.eventuate.jdbc.EventuateLocalAggregateStore
- com.networknt.eventuate.common.impl.AggregateEvents:
  - com.networknt.eventuate.client.KafkaAggregateSubscriptions
- com.networknt.eventuate.common.impl.AggregateCrud:
  - com.networknt.eventuate.common.impl.adapters.SyncToAsyncAggregateCrudAdapter
- com.networknt.eventuate.common.SnapshotManager:
  - com.networknt.eventuate.common.SnapshotManagerImpl
- com.networknt.eventuate.common.MissingApplyEventMethodStrategy:
  - com.networknt.eventuate.common.DefaultMissingApplyEventMethodStrategy
- com.networknt.eventuate.common.EventuateAggregateStore:
  - com.networknt.eventuate.common.impl.EventuateAggregateStoreImpl
- com.networknt.eventuate.todolist.domain.TodoAggregate:
  - com.networknt.eventuate.todolist.domain.TodoAggregate
- com.networknt.eventuate.todolist.domain.TodoBulkDeleteAggregate:
  - com.networknt.eventuate.todolist.domain.TodoBulkDeleteAggregate
- com.networknt.eventuate.todolist.TodoQueryRepository:
  - com.networknt.eventuate.todolist.TodoQueryRepositoryImpl
- com.networknt.eventuate.todolist.TodoQueryService:
  - com.networknt.eventuate.todolist.TodoQueryServiceImpl
- com.networknt.eventuate.todolist.TodoQueryWorkflow:
  - com.networknt.eventuate.todolist.TodoQueryWorkflow
- com.networknt.eventuate.event.EventHandlerProcessor:
  - com.networknt.eventuate.event.EventHandlerProcessorDispatchedEventReturningVoid
  - com.networknt.eventuate.event.EventHandlerProcessorDispatchedEventReturningCompletableFuture
  - com.networknt.eventuate.event.EventHandlerProcessorEventHandlerContextReturningCompletableFuture
  - com.networknt.eventuate.event.EventHandlerProcessorEventHandlerContextReturningVoid
- com.networknt.eventuate.client.SubscriptionsRegistry:
  - com.networknt.eventuate.client.SubscriptionsRegistryImpl

```

As there are test cases and a test server will be started, we need to create a
service.yml in /src/test/resources/config folder to utilize H2 database for
our test cases. 

```yaml
singletons:
- javax.sql.DataSource:
  - com.zaxxer.hikari.HikariDataSource:
      DriverClassName: org.h2.jdbcx.JdbcDataSource
      jdbcUrl: jdbc:h2:~/test
      username: sa
      password: sa
- com.networknt.eventuate.common.impl.sync.AggregateCrud,com.networknt.eventuate.common.impl.sync.AggregateEvents:
    - com.networknt.eventuate.test.jdbc.EventuateEmbeddedTestAggregateStore
- com.networknt.eventuate.common.impl.AggregateCrud:
  - com.networknt.eventuate.common.impl.adapters.SyncToAsyncAggregateCrudAdapter
- com.networknt.eventuate.common.impl.AggregateEvents:
  - com.networknt.eventuate.common.impl.adapters.SyncToAsyncAggregateEventsAdapter
- com.networknt.eventuate.common.SnapshotManager:
  - com.networknt.eventuate.common.SnapshotManagerImpl
- com.networknt.eventuate.common.MissingApplyEventMethodStrategy:
  - com.networknt.eventuate.common.DefaultMissingApplyEventMethodStrategy
- com.networknt.eventuate.common.EventuateAggregateStore:
  - com.networknt.eventuate.common.impl.EventuateAggregateStoreImpl
- com.networknt.eventuate.common.sync.EventuateAggregateStore:
  - com.networknt.eventuate.common.impl.sync.EventuateAggregateStoreImpl
- com.networknt.eventuate.todolist.domain.TodoAggregate:
  - com.networknt.eventuate.todolist.domain.TodoAggregate
- com.networknt.eventuate.todolist.domain.TodoBulkDeleteAggregate:
  - com.networknt.eventuate.todolist.domain.TodoBulkDeleteAggregate
- com.networknt.eventuate.todolist.TodoQueryRepository:
  - com.networknt.eventuate.todolist.TodoQueryRepositoryImpl
- com.networknt.eventuate.todolist.TodoQueryService:
  - com.networknt.eventuate.todolist.TodoQueryServiceImpl
- com.networknt.eventuate.todolist.TodoQueryWorkflow:
  - com.networknt.eventuate.todolist.TodoQueryWorkflow
- com.networknt.eventuate.event.EventHandlerProcessor:
  - com.networknt.eventuate.event.EventHandlerProcessorDispatchedEventReturningVoid
  - com.networknt.eventuate.event.EventHandlerProcessorDispatchedEventReturningCompletableFuture
  - com.networknt.eventuate.event.EventHandlerProcessorEventHandlerContextReturningCompletableFuture
  - com.networknt.eventuate.event.EventHandlerProcessorEventHandlerContextReturningVoid
- com.networknt.eventuate.client.SubscriptionsRegistry:
  - com.networknt.eventuate.client.SubscriptionsRegistryImpl

```

The rest-query will subscribe events from light-eventuate-4j so it need to
config event handler registration in the StartupHookProvider.

```
# This is the place to plugin your startup hooks to initialize Spring application context,
# set up db connection pools and allocate resources.

# config event handle registration
com.networknt.eventuate.client.EventuateClientStartupHookProvider
```

Now let's verify that all modules can be built.

```
mvn clean install
```


## Test restful services

Once all modules can be built successfully, we can start the servers and test the todo-list
application.

First we need to make sure Mysql, Zookeeper, Kafka and CDC server are up and running.

You can follow this [tutorial](https://networknt.github.io/light-eventuate-4j/tutorial/service-dev/) 
to start all of them with docker-compose.

Before we start the rest-command and rest-query, let's create the database and table
for the rest-query material view in the same Mysql database for the event store. We
are going to create another database called todo_db

Here is the db script and you can use mysql command line or just using any GUI tools
to run it against Mysql server.

```mysql
create database todo_db;

GRANT ALL PRIVILEGES ON todo_db.* TO 'mysqluser' IDENTIFIED BY 'mysqlpw';

use todo_db;

DROP table IF EXISTS  TODO;


CREATE  TABLE TODO (
  ID varchar(255),
  TITLE varchar(255),
  COMPLETED BOOLEAN,
  ORDER_ID INTEGER,
  ACTIVE_FLG varchar(1) DEFAULT 'Y',
  PRIMARY KEY(ID)
);


```

Remember that Mysql root user and password as follows.

```
dbUser: root
dbPass: rootpassword

```

Now let's start rest-command service

```
cd ~/networknt/light-example-4j/eventuate/todo-list/rest-command
java -jar target/rest-command-1.0.0.jar
```

Now let's start rest-query service

```
cd ~/networknt/light-example-4j/eventuate/todo-list/rest-query
java -jar target/rest-query-1.0.0.jar
```


Let's create a todo item with curl

```
curl -X POST \
  http://localhost:8083/v1/todos \
  -H 'cache-control: no-cache' \
  -H 'content-type: application/json' \
  -d '{"title":"this is the test todo from postman","completed":false,"order":0}'
```

And the response will be 

```json
{
    "cancelled": false,
    "done": true,
    "completedExceptionally": false,
    "numberOfDependents": 0
}
```

Now let's access the rest-query service

```
curl -X GET http://localhost:8082/v1/todos
```

And the response will be something like this.

```json
[
    {
        "0000015d968eacee-0e23a9398a990001": {
            "title": "this is the test todo from postman",
            "completed": false,
            "order": 0
        }
    },
    {
        "0000015d968f5443-0e23a9398a990001": {
            "title": "this is the test todo from postman",
            "completed": false,
            "order": 0
        }
    },
    {
        "0000015d969d6ce2-0e23a9398a990001": {
            "title": "this is the test todo from postman",
            "completed": false,
            "order": 0
        }
    }
]
```


## Command side hybrid service

So far, we have tried to implement both Command and Query services with Restful
APIs. From this step, we are going to implement these service with light-hybrid-4j
framework. Although we can put cdc-service, hybrid-command, hybrid-query into the
same hybrid server instance, we are going to use two hybrid server instances to
show case Command Query Responsibility Segregation(CQRS).

If we use hybrid framework, we don't need the RESTful CDC service anymore. We can
put the cdc-service into the hybrid-command server.

To create command side hybrid server, we are going to use light-codegen to generate
the project in todo-list. Before generating the project, we need to create a config.json
to control how generator works and a schema.json to define the service contract.

These two files can be found in model-config/hybrid/todo-command folder. 

config.json

```json
{
  "rootPackage": "com.networknt.todolist.command",
  "handlerPackage":"com.networknt.todolist.command.handler",
  "modelPackage":"com.networknt.todolist.command.model",
  "artifactId": "hybrid-command",
  "groupId": "com.networknt",
  "name": "hybrid-command",
  "version": "0.1.0",
  "overwriteHandler": true,
  "overwriteHandlerTest": true,
  "httpPort": 8080,
  "enableHttp": true,
  "httpsPort": 8443,
  "enableHttps": false,
  "enableRegistry": false,
  "supportOracle": false,
  "supportMysql": false,
  "supportPostgresql": false,
  "supportH2ForTest": false,
  "supportClient": false
}

```

schema.json 

```json
{
  "host": "lightapi.net",
  "service": "todo",
  "action": [
   {
    "name": "delete",
    "version": "0.1.0",
    "handler": "DeleteTodo",
    "scope" : "todo.w",    
    "schema" : {
      "title" : "Service",
      "type" : "object",
      "properties" : {
        "id" : {
          "type" : "string"
        }
      },
      "required" : [ "id"]
    },
    "scope" : "todo.w"
  },
  {
      "name": "create",
      "version": "0.1.0",
      "handler": "CreateTodo",
      "scope" : "todo.w",
      "schema" : {
      "title" : "Service",
      "type" : "object",
      "properties" : {
        "title" : {
          "type" : "string"
        },
        "completed" : {
          "type" : "boolean"
        },
        "order" : {
          "description" : "order of todo in the todo list",
          "type" : "integer",
          "minimum" : 0
        }
      },
      "required" : [ "title" ]
    },
    "scope" : "todo.w"
  },
{
      "name": "update",
      "version": "0.1.0",
      "handler": "UpdateTodo",
      "scope" : "todo.w",
      "schema" : {
      "title" : "Service",
      "type" : "object",
      "properties" : {
        "id" : {
          "type" : "string"
        },
        "title" : {
          "type" : "string"
        },
        "completed" : {
          "type" : "boolean"
        },
        "order" : {
          "description" : "order of todo in the todo list",
          "type" : "integer",
          "minimum" : 0
        }
      },
      "required" : [ "id" ]
    },
    "scope" : "todo.w"
  }
  ]
}
```

Now let's run the light-codegen command line to generate the project (get latest light-codegen from master branch).

```
cd ~/networknt/light-codegen

java -jar codegen-cli/target/codegen-cli.jar -f light-hybrid-4j-service -o ../light-example-4j/eventuate/todo-list/hybrid-command -m ../model-config/hybrid/todo-command/schema.json -c ../model-config/hybrid/todo-command/config.json
```

Let's add this newly generated project to the parent pom.xml and build all
modules.

```xml
    <modules>
        <module>common</module>
        <module>command</module>
        <module>query</module>
        <module>rest-command</module>
        <module>rest-query</module>
        <module>hybrid-command</module>
    </modules>
```

```xml
            <dependency>
                <groupId>com.networknt</groupId>
                <artifactId>hybrid-command</artifactId>
                <version>${project.version}</version>
            </dependency>

```

Rebuild all modules from root folder.

```
mvn clean install

```

Now let's change the dependencies to add light-eventuate-4j modules and todo-list
modules.

```xml
        <version.light-eventuate-4j>1.3.5</version.light-eventuate-4j>

```

```xml
        <dependency>
            <groupId>com.networknt</groupId>
            <artifactId>eventuate-common</artifactId>
            <version>${version.light-eventuate-4j}</version>
        </dependency>
        <dependency>
            <groupId>com.networknt</groupId>
            <artifactId>eventuate-jdbc</artifactId>
            <version>${version.light-eventuate-4j}</version>
        </dependency>
        <dependency>
            <groupId>com.networknt</groupId>
            <artifactId>eventuate-client</artifactId>
            <version>${version.light-eventuate-4j}</version>
        </dependency>
        <dependency>
            <groupId>com.networknt</groupId>
            <artifactId>todo-common</artifactId>
            <version>0.1.0</version>
        </dependency>
        <dependency>
            <groupId>com.networknt</groupId>
            <artifactId>todo-command</artifactId>
            <version>0.1.0</version>
        </dependency>

```

With all the dependencies are added, let's change the handler to wire in the
right logic.

```java
package com.networknt.todolist.command.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.config.Config;
import com.networknt.eventuate.common.AggregateRepository;
import com.networknt.eventuate.common.EventuateAggregateStore;
import com.networknt.eventuate.todolist.TodoCommandService;
import com.networknt.eventuate.todolist.TodoCommandServiceImpl;
import com.networknt.eventuate.todolist.common.model.TodoInfo;
import com.networknt.eventuate.todolist.domain.TodoAggregate;
import com.networknt.eventuate.todolist.domain.TodoBulkDeleteAggregate;
import com.networknt.service.SingletonServiceFactory;
import com.networknt.utility.NioUtils;
import com.networknt.rpc.Handler;
import com.networknt.rpc.router.ServiceHandler;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

@ServiceHandler(id="lightapi.net/todo/create/0.1.0")
public class CreateTodo implements Handler {

    private EventuateAggregateStore eventStore  = (EventuateAggregateStore) SingletonServiceFactory.getBean(EventuateAggregateStore.class);

    private AggregateRepository todoRepository = new AggregateRepository(TodoAggregate.class, eventStore);
    private AggregateRepository bulkDeleteAggregateRepository  = new AggregateRepository(TodoBulkDeleteAggregate.class, eventStore);

    private TodoCommandService service = new TodoCommandServiceImpl(todoRepository, bulkDeleteAggregateRepository);

    @Override
    public ByteBuffer handle(Object input)  {

        JsonNode inputPara = Config.getInstance().getMapper().valueToTree(input);

        TodoInfo todo = new TodoInfo();
        todo.setTitle(inputPara.findPath("title").asText());
        todo.setCompleted(inputPara.findPath("completed").asBoolean());
        todo.setOrder(inputPara.findPath("order").asInt());

        CompletableFuture<TodoInfo> result = service.add(todo).thenApply((e) -> {
            TodoInfo m = e.getAggregate().getTodo();
            return m;
        });

        return NioUtils.toByteBuffer("{\"message\":" + result + "}");
    }
}

```

```java
package com.networknt.todolist.command.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.config.Config;
import com.networknt.eventuate.common.AggregateRepository;
import com.networknt.eventuate.common.EventuateAggregateStore;
import com.networknt.eventuate.todolist.TodoCommandService;
import com.networknt.eventuate.todolist.TodoCommandServiceImpl;
import com.networknt.eventuate.todolist.common.model.TodoInfo;
import com.networknt.eventuate.todolist.domain.TodoAggregate;
import com.networknt.eventuate.todolist.domain.TodoBulkDeleteAggregate;
import com.networknt.service.SingletonServiceFactory;
import com.networknt.utility.NioUtils;
import com.networknt.rpc.Handler;
import com.networknt.rpc.router.ServiceHandler;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

@ServiceHandler(id="lightapi.net/todo/delete/0.1.0")
public class DeleteTodo implements Handler {

    private EventuateAggregateStore eventStore  = (EventuateAggregateStore) SingletonServiceFactory.getBean(EventuateAggregateStore.class);

    private AggregateRepository todoRepository = new AggregateRepository(TodoAggregate.class, eventStore);
    private AggregateRepository bulkDeleteAggregateRepository  = new AggregateRepository(TodoBulkDeleteAggregate.class, eventStore);

    private TodoCommandService service = new TodoCommandServiceImpl(todoRepository, bulkDeleteAggregateRepository);


    @Override
    public ByteBuffer handle(Object input)  {

        JsonNode inputPara = Config.getInstance().getMapper().valueToTree(input);
        // delete a todo-event
        String id = inputPara.findPath("id").asText();

        CompletableFuture<TodoInfo> result = service.remove(id).thenApply((e) -> {
            TodoInfo m = e.getAggregate().getTodo();
            return m;
        });


        return NioUtils.toByteBuffer("{\"message\":" + result + "}");
    }
}

```

```java
package com.networknt.todolist.command.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.config.Config;
import com.networknt.eventuate.common.AggregateRepository;
import com.networknt.eventuate.common.EventuateAggregateStore;
import com.networknt.eventuate.todolist.TodoCommandService;
import com.networknt.eventuate.todolist.TodoCommandServiceImpl;
import com.networknt.eventuate.todolist.common.model.TodoInfo;
import com.networknt.eventuate.todolist.domain.TodoAggregate;
import com.networknt.eventuate.todolist.domain.TodoBulkDeleteAggregate;
import com.networknt.service.SingletonServiceFactory;
import com.networknt.utility.NioUtils;
import com.networknt.rpc.Handler;
import com.networknt.rpc.router.ServiceHandler;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

@ServiceHandler(id="lightapi.net/todo/update/0.1.0")
public class UpdateTodo implements Handler {

    private EventuateAggregateStore eventStore  = (EventuateAggregateStore) SingletonServiceFactory.getBean(EventuateAggregateStore.class);

    private AggregateRepository todoRepository = new AggregateRepository(TodoAggregate.class, eventStore);
    private AggregateRepository bulkDeleteAggregateRepository  = new AggregateRepository(TodoBulkDeleteAggregate.class, eventStore);

    private TodoCommandService service = new TodoCommandServiceImpl(todoRepository, bulkDeleteAggregateRepository);
    @Override
    public ByteBuffer handle(Object input)  {

        JsonNode inputPara = Config.getInstance().getMapper().valueToTree(input);

        String id = inputPara.findPath("id").asText();;
        TodoInfo todo = new TodoInfo();
        todo.setTitle(inputPara.findPath("title").asText());
        todo.setCompleted(inputPara.findPath("completed").asBoolean());
        todo.setOrder(inputPara.findPath("order").asInt());

        CompletableFuture<TodoInfo> result = service.update(id, todo).thenApply((e) -> {
            TodoInfo m = e.getAggregate().getTodo();
            return m;
        });


        return NioUtils.toByteBuffer("{\"message\":" + result + "}");
    }
}

```

As we have to put the service jar into the hybrid server platform in order to run
it, we cannot wait then to test it. So for hybrid service development, end-to-end
test is very important to ensure what you have built is working.

```java
package com.networknt.todolist.command.handler;

import com.networknt.client.Client;
import com.networknt.server.Server;
import com.networknt.exception.ClientException;
import com.networknt.exception.ApiException;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.jose4j.json.internal.json_simple.JSONObject;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CreateTodoTest {
    @ClassRule
    public static TestServer server = TestServer.getInstance();

    static final Logger logger = LoggerFactory.getLogger(CreateTodo.class);

    @Test
    public void testCreateTodo() throws ClientException, ApiException {
        CloseableHttpClient client = Client.getInstance().getSyncClient();
        HttpPost httpPost = new HttpPost("http://localhost:8080/api/json");

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("host", "lightapi.net");
        map.put("service", "todo");
        map.put("action", "create");
        map.put("version", "0.1.0");
        map.put("title", "create todo from hybrid service unit test");
        map.put("completed", false);
        map.put("order", 1);
        JSONObject json = new JSONObject();
        json.putAll( map );
        System.out.printf( "JSON: %s", json.toString() );


        //Client.getInstance().addAuthorization(httpPost);
        try {
            httpPost.setEntity(new StringEntity(json.toString()));
            httpPost.setHeader("Content-type", "application/json");
            CloseableHttpResponse response = client.execute(httpPost);
            Assert.assertEquals(200, response.getStatusLine().getStatusCode());

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
```

```java
package com.networknt.todolist.command.handler;

import com.networknt.client.Client;
import com.networknt.server.Server;
import com.networknt.exception.ClientException;
import com.networknt.exception.ApiException;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.jose4j.json.internal.json_simple.JSONObject;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class DeleteTodoTest {
    @ClassRule
    public static TestServer server = TestServer.getInstance();

    static final Logger logger = LoggerFactory.getLogger(DeleteTodo.class);

    @Test
    public void testDeleteTodo() throws ClientException, ApiException {
        CloseableHttpClient client = Client.getInstance().getSyncClient();
        HttpPost httpPost = new HttpPost("http://localhost:8080/api/json");
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("host", "lightapi.net");
        map.put("service", "todo");
        map.put("action", "delete");
        map.put("version", "0.1.0");
        map.put("id", "101010");

        JSONObject json = new JSONObject();
        json.putAll( map );
        System.out.printf( "JSON: %s", json.toString() );


        try {
            httpPost.setEntity(new StringEntity(json.toString()));
            httpPost.setHeader("Content-type", "application/json");
            CloseableHttpResponse response = client.execute(httpPost);
            Assert.assertEquals(200, response.getStatusLine().getStatusCode());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
```

```java
package com.networknt.todolist.command.handler;

import com.networknt.client.Client;
import com.networknt.server.Server;
import com.networknt.exception.ClientException;
import com.networknt.exception.ApiException;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.jose4j.json.internal.json_simple.JSONObject;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class UpdateTodoTest {
    @ClassRule
    public static TestServer server = TestServer.getInstance();

    static final Logger logger = LoggerFactory.getLogger(UpdateTodo.class);

    @Test
    public void testUpdateTodo() throws ClientException, ApiException {
        CloseableHttpClient client = Client.getInstance().getSyncClient();
        HttpPost httpPost = new HttpPost("http://localhost:8080/api/json");

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("host", "lightapi.net");
        map.put("service", "todo");
        map.put("action", "update");
        map.put("version", "0.1.0");
        map.put("id", "101010");
        map.put("title", "create todo from hybrid service unit test");
        map.put("completed", false);
        map.put("order", 1);
        JSONObject json = new JSONObject();
        json.putAll( map );
        System.out.printf( "JSON: %s", json.toString() );


        //Client.getInstance().addAuthorization(httpPost);
        try {
            httpPost.setEntity(new StringEntity(json.toString()));
            httpPost.setHeader("Content-type", "application/json");
            CloseableHttpResponse response = client.execute(httpPost);
            Assert.assertEquals(200, response.getStatusLine().getStatusCode());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

In order to make the test case works, we need to add a service.yml in
src/test/resources/config folder

```yaml
singletons:
- javax.sql.DataSource:
  - com.zaxxer.hikari.HikariDataSource:
      DriverClassName: org.h2.jdbcx.JdbcDataSource
      jdbcUrl: jdbc:h2:~/test
      username: sa
      password: sa
- com.networknt.eventuate.common.impl.sync.AggregateCrud:
  - com.networknt.eventuate.client.EventuateLocalDBAggregateCrud
- com.networknt.eventuate.common.impl.sync.AggregateEvents:
    - com.networknt.eventuate.client.EventuateLocalAggregatesEvents
- com.networknt.eventuate.common.impl.AggregateCrud:
  - com.networknt.eventuate.common.impl.adapters.SyncToAsyncAggregateCrudAdapter
- com.networknt.eventuate.common.impl.AggregateEvents:
  - com.networknt.eventuate.common.impl.adapters.SyncToAsyncAggregateEventsAdapter
- com.networknt.eventuate.common.SnapshotManager:
  - com.networknt.eventuate.common.SnapshotManagerImpl
- com.networknt.eventuate.common.MissingApplyEventMethodStrategy:
  - com.networknt.eventuate.common.DefaultMissingApplyEventMethodStrategy
- com.networknt.eventuate.common.EventuateAggregateStore:
  - com.networknt.eventuate.common.impl.EventuateAggregateStoreImpl
- com.networknt.eventuate.common.sync.EventuateAggregateStore:
  - com.networknt.eventuate.common.impl.sync.EventuateAggregateStoreImpl
- com.networknt.eventuate.todolist.domain.TodoAggregate:
  - com.networknt.eventuate.todolist.domain.TodoAggregate
- com.networknt.eventuate.todolist.domain.TodoBulkDeleteAggregate:
  - com.networknt.eventuate.todolist.domain.TodoBulkDeleteAggregate

```

Before running the tests, we need to add the following test dependencies into
pom.xml 

```xml
        <version.hikaricp>2.5.1</version.hikaricp>
        <version.h2>1.3.176</version.h2>

        <dependency>
            <groupId>com.zaxxer</groupId>
            <artifactId>HikariCP</artifactId>
            <version>${version.hikaricp}</version>
        </dependency>

        <!-- Test Dependencies -->
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <version>${version.h2}</version>
            <scope>test</scope>
        </dependency>

```

Now you can build the entire project from root folder of todo-list

```
mvn clean install
``` 

## Query side hybrid service

To create query side hybrid service, we are going to use light-codegen to generate
the project in todo-list. Before generating the project, we need to create a config.json
to control how generator works and a schema.json to define the service contract.

These two files can be found in model-config/hybrid/todo-query folder. 

config.json

```json
{
  "rootPackage": "com.networknt.todolist.query",
  "handlerPackage":"com.networknt.todolist.query.handler",
  "modelPackage":"com.networknt.todolist.query.model",
  "artifactId": "hybrid-query",
  "groupId": "com.networknt",
  "name": "hybrid-query",
  "version": "0.1.0",
  "overwriteHandler": true,
  "overwriteHandlerTest": true,
  "httpPort": 8080,
  "enableHttp": true,
  "httpsPort": 8443,
  "enableHttps": false,
  "enableRegistry": false,
  "supportOracle": false,
  "supportMysql": false,
  "supportPostgresql": false,
  "supportH2ForTest": false,
  "supportClient": false
}
```

schema.json 

```json
{
  "host": "lightapi.net",
  "service": "todo",
  "action": [
 {
    "name": "gettodo",
    "version": "0.1.0",
    "handler": "GetTodoById",
    "scope" : "todo.r", 
    "schema" : {
      "title" : "Service",
      "type" : "object",
      "properties" : {
        "id" : {
          "type" : "string"
        }
      },
      "required" : [ "id" ]
    },
    "scope" : "todo.r"
  },
 {
     "name": "gettodos",
     "version": "0.1.0",
     "handler": "GetAllTodos",
    "scope" : "todo.r", 
    "schema" : {
      "title" : "Service"
    },
    "scope" : "todo.r"
  }
  ]
}
```

Now let's run the light-codegen command line to generate the project.

```
cd ~/networknt/light-codegen

java -jar codegen-cli/target/codegen-cli.jar -f light-hybrid-4j-service -o ../light-example-4j/eventuate/todo-list/hybrid-query -m ../model-config/hybrid/todo-query/schema.json -c ../model-config/hybrid/todo-query/config.json
```

Let's add this newly generated project to the parent pom.xml and build all
modules.

```xml
    <modules>
        <module>common</module>
        <module>command</module>
        <module>query</module>
        <module>rest-command</module>
        <module>rest-query</module>
        <module>hybrid-command</module>
        <module>hybrid-query</module>
    </modules>
```

```xml
            <dependency>
                <groupId>com.networknt</groupId>
                <artifactId>hybrid-query</artifactId>
                <version>${project.version}</version>
            </dependency>

```

Rebuild all modules from root folder.

```
mvn clean install

```

Now let's change the dependencies to add light-eventuate-4j modules and todo-list
modules.

```xml
        <version.light-eventuate-4j>1.3.5</version.light-eventuate-4j>

```

```xml
        <dependency>
            <groupId>com.networknt</groupId>
            <artifactId>eventuate-common</artifactId>
            <version>${version.light-eventuate-4j}</version>
        </dependency>
        <dependency>
            <groupId>com.networknt</groupId>
            <artifactId>eventuate-jdbc</artifactId>
            <version>${version.light-eventuate-4j}</version>
        </dependency>
        <dependency>
            <groupId>com.networknt</groupId>
            <artifactId>eventuate-client</artifactId>
            <version>${version.light-eventuate-4j}</version>
        </dependency>
        <dependency>
            <groupId>com.networknt</groupId>
            <artifactId>todo-common</artifactId>
            <version>0.1.0</version>
        </dependency>
        <dependency>
            <groupId>com.networknt</groupId>
            <artifactId>todo-command</artifactId>
            <version>0.1.0</version>
        </dependency>
        <dependency>
            <groupId>com.networknt</groupId>
            <artifactId>todo-query</artifactId>
            <version>0.1.0</version>
        </dependency>

```

With all the dependencies are added, let's change the handler to wire in the
right logic.

```java
package com.networknt.todolist.query.handler;

import com.networknt.config.Config;
import com.networknt.eventuate.todolist.TodoQueryService;
import com.networknt.eventuate.todolist.common.model.TodoInfo;
import com.networknt.service.SingletonServiceFactory;
import com.networknt.utility.NioUtils;
import com.networknt.rpc.Handler;
import com.networknt.rpc.router.ServiceHandler;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

@ServiceHandler(id="lightapi.net/todo/gettodos/0.1.0")
public class GetAllTodos implements Handler {
    TodoQueryService service =
            (TodoQueryService) SingletonServiceFactory.getBean(TodoQueryService.class);

    @Override
    public ByteBuffer handle(Object input)  {

        List<Map<String, TodoInfo>> resultAll = service.getAll();
        String returnMessage = null;
        try {
            returnMessage = Config.getInstance().getMapper().writeValueAsString(resultAll);
        } catch ( Exception e) {

        }
        return NioUtils.toByteBuffer("{\"message\":" +returnMessage + "}");

    }
}

```

```java
package com.networknt.todolist.query.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.config.Config;
import com.networknt.eventuate.todolist.TodoQueryService;
import com.networknt.eventuate.todolist.common.model.TodoInfo;
import com.networknt.service.SingletonServiceFactory;
import com.networknt.utility.NioUtils;
import com.networknt.rpc.Handler;
import com.networknt.rpc.router.ServiceHandler;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@ServiceHandler(id="lightapi.net/todo/gettodo/0.1.0")
public class GetTodoById implements Handler {
    TodoQueryService service =
            (TodoQueryService) SingletonServiceFactory.getBean(TodoQueryService.class);

    @Override
    public ByteBuffer handle(Object input) {

        JsonNode inputPara = Config.getInstance().getMapper().valueToTree(input);

        String id = inputPara.findPath("id").asText();


        CompletableFuture<Map<String, TodoInfo>> result = service.findById(id);
        String returnMessage = null;
        try {
            returnMessage = Config.getInstance().getMapper().writeValueAsString(result);
        } catch (Exception e) {

        }
        return NioUtils.toByteBuffer("{\"message\":" + returnMessage + "}");
    }
}

```

As we have to put the service jar into the hybrid server platform in order to run
it, we cannot wait then to test it. So for hybrid service development, end-to-end
test is very important to ensure what you have built is working.

Here are the two tests.

```java
package com.networknt.todolist.query.handler;

import com.networknt.client.Client;
import com.networknt.server.Server;
import com.networknt.exception.ClientException;
import com.networknt.exception.ApiException;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.jose4j.json.internal.json_simple.JSONObject;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class GetAllTodosTest {
    @ClassRule
    public static TestServer server = TestServer.getInstance();

    static final Logger logger = LoggerFactory.getLogger(GetAllTodos.class);

    @Test
    public void testGetAllTodos() throws ClientException, ApiException {
        CloseableHttpClient client = Client.getInstance().getSyncClient();
        HttpPost httpPost = new HttpPost("http://localhost:8082/api/json");
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("host", "lightapi.net");
        map.put("service", "todo");
        map.put("action", "gettodos");
        map.put("version", "0.1.0");

        JSONObject json = new JSONObject();
        json.putAll( map );
        System.out.printf( "JSON: %s", json.toString() );
        //Client.getInstance().addAuthorization(httpPost);
        try {
            httpPost.setEntity(new StringEntity(json.toString()));
            httpPost.setHeader("Content-type", "application/json");
            CloseableHttpResponse response = client.execute(httpPost);
            Assert.assertEquals(200, response.getStatusLine().getStatusCode());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
```

```java
package com.networknt.todolist.query.handler;

import com.networknt.client.Client;
import com.networknt.server.Server;
import com.networknt.exception.ClientException;
import com.networknt.exception.ApiException;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.jose4j.json.internal.json_simple.JSONObject;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class GetTodoByIdTest {
    @ClassRule
    public static TestServer server = TestServer.getInstance();

    static final Logger logger = LoggerFactory.getLogger(GetTodoById.class);

    @Test
    public void testGetTodoById() throws ClientException, ApiException {
        CloseableHttpClient client = Client.getInstance().getSyncClient();
        HttpPost httpPost = new HttpPost("http://localhost:8082/api/json");
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("host", "lightapi.net");
        map.put("service", "todo");
        map.put("action", "gettodo");
        map.put("version", "0.1.0");
        map.put("id", "101010");

        JSONObject json = new JSONObject();
        json.putAll( map );
        System.out.printf( "JSON: %s", json.toString() );

        try {
            httpPost.setEntity(new StringEntity(json.toString()));
            httpPost.setHeader("Content-type", "application/json");
            CloseableHttpResponse response = client.execute(httpPost);
            Assert.assertEquals(200, response.getStatusLine().getStatusCode());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
```

The test server will load some light-eventuate-4j classes and H2 database and it
is defined in service.yml in src/test/resources/config folder.

```yaml
singletons:
- javax.sql.DataSource:
  - com.zaxxer.hikari.HikariDataSource:
      DriverClassName: org.h2.jdbcx.JdbcDataSource
      jdbcUrl: jdbc:h2:~/test
      username: sa
      password: sa
- com.networknt.eventuate.common.impl.sync.AggregateCrud,com.networknt.eventuate.common.impl.sync.AggregateEvents:
    - com.networknt.eventuate.test.jdbc.EventuateEmbeddedTestAggregateStore
- com.networknt.eventuate.common.impl.AggregateCrud:
  - com.networknt.eventuate.common.impl.adapters.SyncToAsyncAggregateCrudAdapter
- com.networknt.eventuate.common.impl.AggregateEvents:
  - com.networknt.eventuate.common.impl.adapters.SyncToAsyncAggregateEventsAdapter
- com.networknt.eventuate.common.SnapshotManager:
  - com.networknt.eventuate.common.SnapshotManagerImpl
- com.networknt.eventuate.common.MissingApplyEventMethodStrategy:
  - com.networknt.eventuate.common.DefaultMissingApplyEventMethodStrategy
- com.networknt.eventuate.common.EventuateAggregateStore:
  - com.networknt.eventuate.common.impl.EventuateAggregateStoreImpl
- com.networknt.eventuate.common.sync.EventuateAggregateStore:
  - com.networknt.eventuate.common.impl.sync.EventuateAggregateStoreImpl
- com.networknt.eventuate.todolist.domain.TodoAggregate:
  - com.networknt.eventuate.todolist.domain.TodoAggregate
- com.networknt.eventuate.todolist.domain.TodoBulkDeleteAggregate:
  - com.networknt.eventuate.todolist.domain.TodoBulkDeleteAggregate
- com.networknt.eventuate.todolist.TodoQueryRepository:
  - com.networknt.eventuate.todolist.TodoQueryRepositoryImpl
- com.networknt.eventuate.todolist.TodoQueryService:
  - com.networknt.eventuate.todolist.TodoQueryServiceImpl
- com.networknt.eventuate.todolist.TodoQueryWorkflow:
  - com.networknt.eventuate.todolist.TodoQueryWorkflow
- com.networknt.eventuate.event.EventHandlerProcessor:
  - com.networknt.eventuate.event.EventHandlerProcessorDispatchedEventReturningVoid
  - com.networknt.eventuate.event.EventHandlerProcessorDispatchedEventReturningCompletableFuture
  - com.networknt.eventuate.event.EventHandlerProcessorEventHandlerContextReturningCompletableFuture
  - com.networknt.eventuate.event.EventHandlerProcessorEventHandlerContextReturningVoid
- com.networknt.eventuate.client.SubscriptionsRegistry:
  - com.networknt.eventuate.client.SubscriptionsRegistryImpl

```

Before running the tests, we need to add the following test dependencies into
pom.xml 

```xml
        <version.hikaricp>2.5.1</version.hikaricp>
        <version.h2>1.3.176</version.h2>

        <dependency>
            <groupId>com.zaxxer</groupId>
            <artifactId>HikariCP</artifactId>
            <version>${version.hikaricp}</version>
        </dependency>

        <!-- Test Dependencies -->
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <version>${version.h2}</version>
            <scope>test</scope>
        </dependency>

```

Now you can build the entire project from root folder of todo-list

```
mvn clean install
``` 


## Test hybrid services

We will start hybrid-command server and hybrid-query server manually first and
then start them together along with services in a docker-compose.

The first step is to make sure light-eventuate-4j platform is up and running.
Please follow this [tutorial](https://networknt.github.io/light-eventuate-4j/tutorial/service-dev/)

To start hybrid servers manually, we need to checkout light-eventuate-4j as these
two servers are part of it.

```
cd ~/networknt
git clone git@github.com:networknt/light-eventuate-4j.git
cd light-eventuate-4j
mvn clean install
```

Next let's start hybrid-command server with cdc-service and todo-list hybrid-command
service. 

As there are more jars that need to be on the classpath of hybrid-command server
we are going to copy all of them into light-docker/

```
cd ~/networknt/light-docker/eventuate/hybrid-command/service

cp ~/networknt/light-example-4j/eventuate/todo-list/common/target/todo-common-0.1.0.jar .
cp ~/networknt/light-example-4j/eventuate/todo-list/command/target/todo-command-0.1.0.jar .
cp ~/networknt/light-example-4j/eventuate/todo-list/hybrid-command/target/hybrid-command-0.1.0.jar .
```

```
cd ~/networknt/light-eventuate-4j/hybrid-command

mvn clean install

java -cp ~/networknt/light-docker/eventuate/hybrid-command/service/*:target/hybrid-command-1.3.5.jar com.networknt.server.Server

```


Next let's start hybrid-query server with todo-list hybrid-query
service. 

As there are more jars that need to be on the classpath of hybrid-query server
we are going to copy all of them into light-docker/

```
cd ~/networknt/light-docker/eventuate/hybrid-query/service
cp ~/networknt/light-example-4j/eventuate/todo-list/common/target/todo-common-0.1.0.jar .
cp ~/networknt/light-example-4j/eventuate/todo-list/command/target/todo-command-0.1.0.jar .
cp ~/networknt/light-example-4j/eventuate/todo-list/query/target/todo-query-0.1.0.jar .
cp ~/networknt/light-example-4j/eventuate/todo-list/hybrid-query/target/hybrid-query-0.1.0.jar .
```

```
cd ~/networknt/light-eventuate-4j/hybrid-query

mvn clean install

java -cp ~/networknt/light-docker/eventuate/hybrid-query/service/*:target/hybrid-query-1.3.5.jar com.networknt.server.Server

```

Now let's create a todo item on command service with the following command.

```
curl -X POST \
  http://localhost:8083/api/json \
  -H 'cache-control: no-cache' \
  -H 'content-type: application/json' \
  -d '{"host":"lightapi.net","service":"todo","action":"create", "version": "0.1.0", "title": "create todo item from hybrid-command", "completed": false, "order": 1}'
```

Let's get all the todo items from query service with the following command

```
curl -X POST \
  http://localhost:8082/api/json \
  -H 'cache-control: no-cache' \
  -H 'content-type: application/json' \
  -d '{"host":"lightapi.net","service":"todo","action":"gettodos", "version": "0.1.0"}'
```

## Dockerization

## Restful Service API:

Let's copy docker config folder to the todo-list folder from existing
one.

- docker  contains docker config files


```
cd ~/networknt/light-example-4j/eventuate/todo-list
cp -r ../todo-list.bak/docker .

```

There are two sub-folders in the docker module:


\command-service      -- command side config, include service.yml file

Since the service runs in the docker container, we need change the mysql host name to use docker image name:

service.yml:

```yaml
- javax.sql.DataSource:
  - com.zaxxer.hikari.HikariDataSource:
      DriverClassName: com.mysql.jdbc.Driver
      jdbcUrl: jdbc:mysql://mysql:3306/eventuate?useSSL=false
      username: mysqluser
      password: mysqlpw

```

\query-service        -- query side config, include service.yml and kafka.yml file

Since the service runs in the docker container, we need change the mysql host name and kafka host name to use docker image name:

service.yml:

```yaml
- javax.sql.DataSource:
  - com.zaxxer.hikari.HikariDataSource:
      DriverClassName: com.mysql.jdbc.Driver
      jdbcUrl: jdbc:mysql://mysql:3306/todo_db?useSSL=false
      username: mysqluser
      password: mysqlpw

```

kafka.yml:

```yaml
bootstrapServers: kafka:9092

```

## Start command side and query side service from docker compose

Instead of start service from command line, start service from docker compose file:

```
cd ~/networknt/light-example-4j/eventuate/todo-list
docker-compose up

```

Then following same steps above to verify the service result from command line



## Restful Service API:

Copy service jar files to service folder:

For hybrid command service :

```
cd ~/networknt/light-docker/eventuate/hybrid-command/service

cp ~/networknt/light-example-4j/eventuate/todo-list/common/target/todo-common-0.1.0.jar .
cp ~/networknt/light-example-4j/eventuate/todo-list/command/target/todo-command-0.1.0.jar .
cp ~/networknt/light-example-4j/eventuate/todo-list/hybrid-command/target/hybrid-command-0.1.0.jar .
cp ~/networknt/light-eventuate-4j/hybrid-command/target/hybrid-command-1.3.5.jar .
```

For hybrid query:

```
cd ~/networknt/light-docker/eventuate/hybrid-query/service
cp ~/networknt/light-example-4j/eventuate/todo-list/common/target/todo-common-0.1.0.jar .
cp ~/networknt/light-example-4j/eventuate/todo-list/command/target/todo-command-0.1.0.jar .
cp ~/networknt/light-example-4j/eventuate/todo-list/query/target/todo-query-0.1.0.jar .
cp ~/networknt/light-example-4j/eventuate/todo-list/hybrid-query/target/hybrid-query-0.1.0.jar .
cp ~/networknt/light-eventuate-4j/hybrid-query/target/hybrid-query-1.3.5.jar .

```

Go to light-docker root folder and start docker-compose file for eventutate hybrid service:

```
cd ~/networknt/light-docker/

docker-compose -f docker-compose-eventuate-hybrid-local.yml up

```

Verify result with following command

Create a todo item on command service with the following command.

```
curl -X POST \
  http://localhost:8083/api/json \
  -H 'cache-control: no-cache' \
  -H 'content-type: application/json' \
  -d '{"host":"lightapi.net","service":"todo","action":"create", "version": "0.1.0", "title": "create todo item from hybrid-command", "completed": false, "order": 1}'
```

Let's get all the todo items from query service with the following command

```
curl -X POST \
  http://localhost:8082/api/json \
  -H 'cache-control: no-cache' \
  -H 'content-type: application/json' \
  -d '{"host":"lightapi.net","service":"todo","action":"gettodos", "version": "0.1.0"}'
```