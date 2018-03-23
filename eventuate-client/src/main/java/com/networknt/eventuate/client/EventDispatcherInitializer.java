package com.networknt.eventuate.client;

import com.networknt.eventuate.common.*;
import com.networknt.eventuate.event.EventDispatcher;
import com.networknt.eventuate.event.EventHandler;
import com.networknt.eventuate.event.EventHandlerProcessor;
import com.networknt.eventuate.event.SwimlaneBasedDispatcher;
import com.networknt.eventuate.eventhandling.exceptionhandling.EventDeliveryExceptionHandler;
import com.networknt.eventuate.eventhandling.exceptionhandling.EventDeliveryExceptionHandlerManagerImpl;
import com.networknt.utility.StringUtil;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.fasterxml.jackson.databind.util.ClassUtil.getDeclaredMethods;
import static java.util.stream.Collectors.toList;

/**
 * General process to register the event handles and dispatcher the event to eventHandle processor,
 *
 */
public class EventDispatcherInitializer {

  private EventHandlerProcessor[] processors;
  private EventuateAggregateStore aggregateStore;
  private Executor executorService;
  private SubscriptionsRegistry subscriptionsRegistry;

  private Set<String> subscriberIds = new HashSet<>();

  public EventDispatcherInitializer(EventHandlerProcessor[] processors, EventuateAggregateStore aggregateStore, Executor executorService, SubscriptionsRegistry subscriptionsRegistry) {
    this.processors = processors;
    this.aggregateStore = aggregateStore;
    this.executorService = executorService;
    this.subscriptionsRegistry = subscriptionsRegistry;
  }

  /**
   * Perform the given callback operation on all matching methods of the given
   * class and superclasses (or given interface and super-interfaces).
   * <p>The same named method occurring on subclass and superclass will appear twice.
   *
   * @param clazz the class to introspect
   * @param mc    the callback to invoke for each method
   * @throws IllegalStateException if introspection fails
   */
  public static void doWithMethods(Class<?> clazz, MethodCallback mc) {
    // Keep backing up the inheritance hierarchy.
    Method[] methods = getDeclaredMethods(clazz);
    for (Method method : methods) {
      try {
        mc.doWith(method);
      } catch (IllegalAccessException ex) {
        throw new IllegalStateException("Not allowed to access method '" + method.getName() + "': " + ex);
      }
    }
    if (clazz.getSuperclass() != null) {
      doWithMethods(clazz.getSuperclass(), mc);
    } else if (clazz.isInterface()) {
      for (Class<?> superIfc : clazz.getInterfaces()) {
        doWithMethods(superIfc, mc);
      }
    }
  }

  /**
   * Get the unique set of declared methods on the leaf class and all superclasses.
   * Leaf class methods are included first and while traversing the superclass hierarchy
   * any methods found with signatures matching a method already included are filtered out.
   *
   * @param leafClass the class to introspect
   * @return Method[] an array of Method
   * @throws IllegalStateException if introspection fails
   */
  public static Method[] getUniqueDeclaredMethods(Class<?> leafClass) {
    final List<Method> methods = new ArrayList<>(32);
    doWithMethods(leafClass, new MethodCallback() {
      @Override
      public void doWith(Method method) {
        boolean knownSignature = false;
        Method methodBeingOverriddenWithCovariantReturnType = null;
        for (Method existingMethod : methods) {
          if (method.getName().equals(existingMethod.getName()) &&
                  Arrays.equals(method.getParameterTypes(), existingMethod.getParameterTypes())) {
            // Is this a covariant return type situation?
            if (existingMethod.getReturnType() != method.getReturnType() &&
                    existingMethod.getReturnType().isAssignableFrom(method.getReturnType())) {
              methodBeingOverriddenWithCovariantReturnType = existingMethod;
            } else {
              knownSignature = true;
            }
            break;
          }
        }
        if (methodBeingOverriddenWithCovariantReturnType != null) {
          methods.remove(methodBeingOverriddenWithCovariantReturnType);
        }
        if (!knownSignature) {
          methods.add(method);
        }
      }
    });
    return methods.toArray(new Method[methods.size()]);
  }

  public interface MethodCallback {

    /**
     * Perform an operation using the given method.
     *
     * @param method the method to operate on
     * @throws IllegalArgumentException illegal argument exception
     * @throws IllegalAccessException illegal access exception
     */
    void doWith(Method method) throws IllegalArgumentException, IllegalAccessException;
  }

  public void registerEventHandler(Object eventHandlerBean, String beanName)  throws ClassNotFoundException {

    List<AccessibleObject> fieldsAndMethods = Stream.<AccessibleObject>concat(Arrays.stream(getUniqueDeclaredMethods( Class.forName(beanName))),
            Arrays.stream(eventHandlerBean.getClass().getDeclaredFields()))
            .collect(Collectors.toList());

    List<AccessibleObject> annotatedCandidateEventHandlers = fieldsAndMethods.stream()
            .filter(fieldOrMethod -> fieldOrMethod.getAnnotation(EventHandlerMethod.class) != null)
            .collect(Collectors.toList());

    List<EventHandler> handlers = annotatedCandidateEventHandlers.stream()
            .map(fieldOrMethod -> Arrays.stream(processors).filter(processor -> processor.supports(fieldOrMethod)).findFirst().orElseThrow(() -> new RuntimeException("Don't know what to do with fieldOrMethod " + fieldOrMethod))
                    .process(eventHandlerBean, fieldOrMethod))
            .collect(Collectors.toList());

    Map<String, Set<String>> aggregatesAndEvents = makeAggregatesAndEvents(handlers.stream()
            .filter(handler -> !handler.getEventType().equals(EndOfCurrentEventsReachedEvent.class)).collect(Collectors.toList()));

    Map<Class<?>, EventHandler> eventTypesAndHandlers = makeEventTypesAndHandlers(handlers);

    List<EventDeliveryExceptionHandler> exceptionHandlers = Arrays.stream(eventHandlerBean.getClass()
            .getDeclaredFields())
            .filter(this::isExceptionHandlerField)
            .map(f -> {
              try {
                f.setAccessible(true);
                return (EventDeliveryExceptionHandler) f.get(eventHandlerBean);
              } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
              }
            })
            .collect(toList());

    EventSubscriber a = null;
    try {
      a = AnnotationUtils.findAnnotation( Class.forName(beanName), EventSubscriber.class);
    }catch (ClassNotFoundException e ) {}


     if (a == null)
      throw new RuntimeException("Needs @EventSubscriber annotation: " + eventHandlerBean);

    String subscriberId = StringUtil.isBlank(a.id()) ? beanName : a.id();

    EventDispatcher eventDispatcher = new EventDispatcher(subscriberId, eventTypesAndHandlers, new EventDeliveryExceptionHandlerManagerImpl(exceptionHandlers));

    SwimlaneBasedDispatcher swimlaneBasedDispatcher = new SwimlaneBasedDispatcher(subscriberId, executorService);


    if (subscriberIds.contains(subscriberId))
      throw new RuntimeException("Duplicate subscriptionId " + subscriberId);
    subscriberIds.add(subscriberId);

    SubscriberOptions subscriberOptions = new SubscriberOptions(a.durability(), a.readFrom(), a.progressNotifications());

    // TODO - it would be nice to do this in parallel
    try {
      aggregateStore.subscribe(subscriberId, aggregatesAndEvents,
              subscriberOptions, de -> swimlaneBasedDispatcher.dispatch(de, eventDispatcher::dispatch)).get(20, TimeUnit.SECONDS);
      subscriptionsRegistry.add(new RegisteredSubscription(subscriberId, aggregatesAndEvents, eventHandlerBean.getClass()));
    } catch (InterruptedException | TimeoutException | ExecutionException e) {
      throw new EventuateSubscriptionFailedException(subscriberId, e);
    }
  }

  private boolean isExceptionHandlerField(Field f) {
    return EventDeliveryExceptionHandler.class.isAssignableFrom(f.getType());
  }

  private Map<Class<?>, EventHandler> makeEventTypesAndHandlers(List<EventHandler> handlers) {
    // TODO - if keys are not unique you get an IllegalStateException
    // Need to provide a helpful error message
    return handlers.stream().collect(Collectors.toMap(EventHandler::getEventType, eh -> eh));
  }

  private Map<String, Set<String>> makeAggregatesAndEvents(List<EventHandler> handlers) {
    return handlers.stream().collect(Collectors.toMap(
            eh -> EventEntityUtil.toEntityTypeName(eh.getEventType()),
            eh -> Collections.singleton(eh.getEventType().getName()),
            (e1, e2) -> {
              HashSet<String> r = new HashSet<String>(e1);
              r.addAll(e2);
              return r;
            }
    ));
  }

}
