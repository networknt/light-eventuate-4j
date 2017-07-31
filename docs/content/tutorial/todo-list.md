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

```$xslt
cd ~/networknt
git clone git@github.com:networknt/light-example-4j.git
cd light-example-4j/eventuate
mv todo-list todo-list.bak
``` 

Once all done, the todo-list project in light-example-4j/eventuate will contain POJOs 
, two restful services and two hybrid services.  

## Create a multiple modules maven project

Let's create a todo-list folder and copy the pom.xml from the existing project.
```$xslt
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
 

```$xslt
cd ~/networknt/light-example-4j/eventuate/todo-list
cp -r ../todo-list.bak/common .
cp -r ../todo-list.bak/command .
cp -r ../todo-list.bak/query .

```

Now let's open pom.xml to remove other modules and only leave common, command and query.

Let's run the maven build to make sure these three modules can be built.

```$xslt
cd ~/networknt/light-example-4j/eventuate/todo-list
mvn clean install
```

## Command side restful service

In this step we are going to generate a command side restful microserivce. The model-config
for this service can be found at model-config repo.

Let's first checkout model-config repo from github.

```$xslt
git clone git@github.com:networknt/model-config.git

```

The swagger specification for todo-list command side service can be found at
~/networknt/model-config/rest/todo-command. In the same folder, there is config.json
which is used to control how light-codegen going to generate the code.

In order to generate the rest-command service, let's first clone the light-codegen
project and build it.

```$xslt
cd ~/networknt
git clone git@github.com:networknt/light-codegen.git
cd light-codegen
mvn clean install
```

Generate rest-command service with the following command line. Assume we are still in
light-codegen folder.

```$xslt
java -jar codegen-cli/target/codegen-cli.jar -f light-rest-4j -o ../light-example-4j/eventuate/todo-list/rest-command -m ../model-config/rest/todo_command/1.0.0/swagger.json -c ../model-config/rest/todo_command/1.0.0/config.json
``` 

Now add this rest-command into pom.xml and build it with maven.

```$xslt
    <modules>
        <module>common</module>
        <module>command</module>
        <module>query</module>
        <module>rest-command</module>
    </modules>

```

```$xslt
mvn clean install
```

The four modules should be built successfully.

Now let's update rest-command module to wire the logic.

First we need to update dependencies for this project by adding the following.
```$xslt
        <version.light-eventuate-4j>1.3.4</version.light-eventuate-4j>


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

```$xslt
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

```$xslt
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

```$xslt
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

```$xslt
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

```$xslt
mvn clean install
```


## Query side restful service

The swagger specification for todo-list query side service can be found at
~/networknt/model-config/rest/todo-query. In the same folder, there is config.json
which is used to control how light-codegen going to generate the code.

Generate rest-query service with the following command line. Assume we are still in
light-codegen folder.

```$xslt
java -jar codegen-cli/target/codegen-cli.jar -f light-rest-4j -o ../light-example-4j/eventuate/todo-list/rest-query -m ../model-config/rest/todo_query/1.0.0/swagger.json -c ../model-config/rest/todo_query/1.0.0/config.json
``` 

Now add this rest-query into parent pom.xml and build it with maven.

```$xslt
    <modules>
        <module>common</module>
        <module>command</module>
        <module>query</module>
        <module>rest-command</module>
        <module>rest-query</module>
    </modules>

```

```$xslt
mvn clean install
```

The five modules should be built successfully.

Now let's update rest-query module to wire the logic.

First we need to update dependencies for this project by adding the following.
```$xslt
        <version.light-eventuate-4j>1.3.4</version.light-eventuate-4j>


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

```$xslt
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


```$xslt
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

As rest-query is going to access Mysql database for material view. A class will
be added and put into service.yml below.

```$xslt
package com.networknt.todolist.restquery;

import com.networknt.eventuate.common.EventContext;
import com.networknt.eventuate.common.SubscriberOptions;
import com.networknt.eventuate.common.impl.EventIdTypeAndData;
import com.networknt.eventuate.common.impl.SerializedEvent;
import com.networknt.eventuate.common.impl.sync.AggregateCrud;
import com.networknt.eventuate.common.impl.sync.AggregateEvents;
import com.networknt.eventuate.jdbc.AbstractEventuateJdbcAggregateStore;

import javax.sql.DataSource;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

public class EventuateLocalTestAggregateStore extends AbstractEventuateJdbcAggregateStore
        implements AggregateCrud, AggregateEvents {

  private AtomicLong eventOffset = new AtomicLong();
  private final Map<String, List<Subscription>> aggregateTypeToSubscription = new HashMap<>();

  public EventuateLocalTestAggregateStore(DataSource dataSource) {
    super(dataSource);
  }


  protected void publish(String aggregateType, String aggregateId, List<EventIdTypeAndData> eventsWithIds) {
    System.out.println("Handler:" +  aggregateTypeToSubscription.size());
    synchronized (aggregateTypeToSubscription) {
      List<Subscription> subscriptions = aggregateTypeToSubscription.get(aggregateType);
      if (subscriptions != null)
        for (Subscription subscription : subscriptions) {
          for (EventIdTypeAndData event : eventsWithIds) {
            if (subscription.isInterestedIn(aggregateType, event.getEventType()))
              subscription.handler.apply(new SerializedEvent(event.getId(), aggregateId, aggregateType, event.getEventData(), event.getEventType(),
                      aggregateId.hashCode() % 8,
                      eventOffset.getAndIncrement(),
                      new EventContext(event.getId().asString()), event.getMetadata()));
          }
        }
    }

  }


  class Subscription {

    private final String subscriberId;
    private final Map<String, Set<String>> aggregatesAndEvents;
    private final Function<SerializedEvent, CompletableFuture<?>> handler;

    public Subscription(String subscriberId, Map<String, Set<String>> aggregatesAndEvents, Function<SerializedEvent, CompletableFuture<?>> handler) {

      this.subscriberId = subscriberId;
      this.aggregatesAndEvents = aggregatesAndEvents;
      this.handler = handler;
    }

    public boolean isInterestedIn(String aggregateType, String eventType) {
      return aggregatesAndEvents.get(aggregateType) != null && aggregatesAndEvents.get(aggregateType).contains(eventType);
    }
  }

  @Override
  public void subscribe(String subscriberId, Map<String, Set<String>> aggregatesAndEvents, SubscriberOptions options, Function<SerializedEvent, CompletableFuture<?>> handler) {
    // TODO handle options
    Subscription subscription = new Subscription(subscriberId, aggregatesAndEvents, handler);
    synchronized (aggregateTypeToSubscription) {
      for (String aggregateType : aggregatesAndEvents.keySet()) {
        List<Subscription> existing = aggregateTypeToSubscription.get(aggregateType);
        if (existing == null) {
          existing = new LinkedList<>();
          aggregateTypeToSubscription.put(aggregateType, existing);
          System.out.println("add Handler:" +  existing.size());

        }
        existing.add(subscription);
      }
    }
  }


}

```

Since we are going to create some eventuate class instances from interfaces. Let's create a
service.yml under src/main/resources/config folder.

```$xslt
singletons:
- javax.sql.DataSource:
  - com.zaxxer.hikari.HikariDataSource:
      DriverClassName: com.mysql.jdbc.Driver
      jdbcUrl: jdbc:mysql://localhost:3306/todo_db?useSSL=false
      username: mysqluser
      password: mysqlpw
- com.networknt.eventuate.common.impl.sync.AggregateCrud:
  - com.networknt.todolist.restquery.EventuateLocalTestAggregateStore
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

```$xslt
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

```$xslt
# This is the place to plugin your startup hooks to initialize Spring application context,
# set up db connection pools and allocate resources.

# config event handle registration
com.networknt.eventuate.client.EventuateClientStartupHookProvider
```

Now let's verify that all modules can be built.

```$xslt
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

```$xslt
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

```$xslt
dbUser: root
dbPass: rootpassword

```

Now let's start rest-command service

```$xslt
cd ~/networknt/light-example-4j/eventuate/todo-list/rest-command
java -jar target/rest-command-1.0.0.jar
```

Now let's start rest-query service

```$xslt
cd ~/networknt/light-example-4j/eventuate/todo-list/rest-query
java -jar target/rest-query-1.0.0.jar
```


Let's create a todo item with curl

```$xslt
curl -X POST \
  http://localhost:8083/v1/todos \
  -H 'cache-control: no-cache' \
  -H 'content-type: application/json' \
  -d '{"title":"this is the test todo from postman","completed":false,"order":0}'
```

And the response will be 

```$xslt
{
    "cancelled": false,
    "done": true,
    "completedExceptionally": false,
    "numberOfDependents": 0
}
```

Now let's access the rest-query service

```$xslt
curl -X GET http://localhost:8082/v1/todos
```

And the response will be something like this.

```$xslt
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

## Query side hybrid service

## Test hybrid services

## Test with todo-list example application

 Prepare query side DB script: (DB script saved at: /light-eventuate-example/mysql; root user for mysql: root/rootpassword)


    create database todo_db;

    CREATE  TABLE TODO (
      ID varchar(255),
      TITLE varchar(255),
      COMPLETED BOOLEAN,
      ORDER_ID INTEGER,
      ACTIVE_FLG varchar(1) DEFAULT 'Y',
      PRIMARY KEY(ID)
    );

    GRANT ALL PRIVILEGES ON todo_db.* TO 'mysqluser' IDENTIFIED BY 'mysqlpw';

 Go query-service module, run command line:
   -- mvn package exec:exec
