---
date: 2016-10-22T20:22:34-04:00
title: TodoList
---

# Introduction

This example can be found at [https://github.com/networknt/light-eventuate-example/todo](https://github.com/networknt/light-eventuate-example/todo)

The TodoList application is the entry point POC example application for the Light Eventuate. It illustrates how you can use the platform to write an application with a microservices architecture that uses Event Sourcing and Command Query Responsibility Segregation (CQRS). The TodoList application lets users maintain a todolist.

The TodoList application is a Java application built on Light-Java platform by using Event Sourcing based programming model. Todos are implemented by an Event Sourcing-based TodoAggregate. The aggregate's events are persisted in the Eventuate event store. The application also maintains a materialized view of the data in MySQL

There are five parts in this projects:

* common module define domain object and event object across module both command side and query side

* Command side API implemented on top of light-eventuate-4j to build and publish events.
* Command side microservice (Restful based or hybrid based) to trigger command API
* Query side API implemented on top of light-eventuate-4j to define the event handles and subscribe events and process by defined event handles.
* Query side microservice (Restful based or hybrid based) to trigger query API.



# Common module

common module define domain object and event object across module both command side and query side

The top level event class define entity annotations:

@EventEntity(entity = "com.networknt.eventuate.todolist.domain.TodoAggregate")
public interface TodoEvent extends Event {
}

by default, light event sourcing framework will use the annotation defined "com.networknt.eventuate.todolist.domain.TodoAggregate" as entity type for entity table and topic name for Kafka.


#Command side API

Command side API implement command to process command and apply events. For todolist sample, it simply return TodoInfo object:

public class TodoAggregate extends ReflectiveMutableCommandProcessingAggregate<TodoAggregate, TodoCommand> {

    private TodoInfo todo;

    private boolean deleted;

    public List<Event> process(CreateTodoCommand cmd) {
        if (this.deleted) {
            return Collections.emptyList();
        }
        return EventUtil.events(new TodoCreatedEvent(cmd.getTodo()));
    }

    public List<Event> process(UpdateTodoCommand cmd) {
        if (this.deleted) {
            return Collections.emptyList();
        }
        return EventUtil.events(new TodoUpdatedEvent(cmd.getTodo()));
    }

    public List<Event> process(DeleteTodoCommand cmd) {
        if (this.deleted) {
            return Collections.emptyList();
        }
        return EventUtil.events(new TodoDeletedEvent());
    }


    public void apply(TodoCreatedEvent event) {
        this.todo = event.getTodo();
    }

    public void apply(TodoUpdatedEvent event) {
        this.todo = event.getTodo();
    }

    public void apply(TodoDeletedEvent event) {
        this.deleted = true;
    }

    public TodoInfo getTodo() {
        return todo;
    }

}




#Command side microservice

Command side microservice is light-4j based microservice:

https://networknt.github.io/light-4j/tutorials/microservices/

It initially generated from swaggen specification based on light-codegen. The service generated resfful based microservice to expose API to external which can be call and trigger event sourcing system.

For example, exposed request API:

    public HttpHandler getHandler() {
        HttpHandler handler = Handlers.routing()
            .add(Methods.DELETE, "/v1/todos/{id}", new TodosIdDeleteHandler())
            .add(Methods.POST, "/v1/todos", new TodosPostHandler())
            .add(Methods.PUT, "/v1/todos", new TodosPutHandler())
        ;
        return handler;
    }

And then in the handler implementation, it will can command side service to publish events:

    public void handleRequest(HttpServerExchange exchange) throws Exception {
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



#Query side API

Command side API implement event subscrible and process.

it defined event handler for process events:

@EventSubscriber(id = "todoQuerySideEventHandlers")
public class TodoQueryWorkflow {

    private TodoQueryService service =
            (TodoQueryService)SingletonServiceFactory.getBean(TodoQueryService.class);

    public TodoQueryWorkflow() {
    }

    @EventHandlerMethod
    public void create(DispatchedEvent<TodoCreatedEvent> de) {
        TodoInfo todo = de.getEvent().getTodo();
        service.save(de.getEntityId(), todo);
    }

    @EventHandlerMethod
    public void delete(DispatchedEvent<TodoDeletedEvent> de) {
        service.remove(de.getEntityId());
    }

    @EventHandlerMethod
    public void update(DispatchedEvent<TodoUpdatedEvent> de) {
        TodoInfo todo = de.getEvent().getTodo();
        service.save(de.getEntityId(), todo);
    }
}
The framework will based on the event handler definition the decide which handler will be used to process the events.

# Query side microservice
Query side microservice is light-4j based microservice:

https://networknt.github.io/light-4j/tutorials/microservices/

It initially generated from swaggen specification based on light-codegen. The service generated resfful based microservice to expose API to external which can be call and subscrible the events from event sourcing system.



## Integration Test

From the root folder of the todo-list project: /light-eventuate-example/todo-list, use maven to build the project:

cmd: mvn clean install

```

```

Copy the command side serivce jar (eventuate-todo-command-service-0.1.0.jar) to framework's command serivce folder:

/light-eventuate-4j/command/service.

Start command side service:

```
java -cp ./service/*:target/eventuate-command-1.3.0.jar com.networknt.server.Server
```

```

Copy the query side serivce jar (eventuate-todo-query-service-0.1.0.jar) to framework's query serivce folder:

/light-eventuate-4j/query/service.

Start query side service:

```
java -cp ./service/*:target/eventuate-query-1.3.0.jar com.networknt.server.Server
```

Open Postman, send POST request to URL:http://localhost:8083/v1/todos
with Json format request body:
{"title":" this is the  todo send from postman","completed":false,"order":0}

It should get response:

{
  "completedExceptionally": false,
  "numberOfDependents": 0,
  "cancelled": false,
  "done": true
}

The request will publish a "CreateTodo" event and will save the entity/event to the event store mysql database.
 we can use sql to verify:

 select * from entity;

 select * from events;

The light event sourcing framework will trigger cdc, and publish the event to stream system (Kafka).

And query side service will get the event from kafka and process the event by event handle. For Todo application the query side service will save the todo list to local TODO table.

From the brower or Postman, send GET request: http://localhost:8083/v1/todos
we will see the TODO-list return from local TODO table.



## End