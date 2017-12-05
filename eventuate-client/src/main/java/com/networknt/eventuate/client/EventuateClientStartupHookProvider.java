package com.networknt.eventuate.client;

import com.networknt.config.Config;
import com.networknt.eventuate.common.EventSubscriber;
import com.networknt.eventuate.common.EventuateAggregateStore;
import com.networknt.eventuate.event.EventHandlerProcessor;
import com.networknt.server.StartupHookProvider;
import com.networknt.service.SingletonServiceFactory;
import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import io.github.lukehutch.fastclasspathscanner.scanner.ClassInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Eventuate Client  StartupHookProvider. start Eventuate event handler process service
 */
public class EventuateClientStartupHookProvider implements StartupHookProvider {
    static final private Logger logger = LoggerFactory.getLogger(EventuateClientStartupHookProvider.class);

    static final String CONFIG_NAME = "eventuate-client";

    static EventuateClientConfig config = (EventuateClientConfig) Config.getInstance().getJsonObjectConfig(CONFIG_NAME, EventuateClientConfig.class);

    static Map<String, ClassInfo> classNameToClassInfo =
            new FastClasspathScanner(config.getHandlerPackage()).scan().getClassNameToClassInfo();

    public void onStartup() {
        // Initialize event dispatcher.
        EventDispatcherInitializer eventDispatcherInitializer = new EventDispatcherInitializer(
                SingletonServiceFactory.getBeans(EventHandlerProcessor.class),
                SingletonServiceFactory.getBean(EventuateAggregateStore.class),
                Executors.newCachedThreadPool(),
                SingletonServiceFactory.getBean(SubscriptionsRegistry.class));

        // lookup all EventSubscriber and register them to subscribe event.
        List<String> subscribers =
                classNameToClassInfo.values().stream()
                        .filter(ci -> ci.hasAnnotation(EventSubscriber.class.getName()))
                        .map(ClassInfo::getClassName)
                        .sorted()
                        .collect(Collectors.toList());

        // for each subscriber, create instance and register.
        for(String className: subscribers) {
            try {
                Class c = Class.forName(className);
                Object subscriber = c.newInstance();
                eventDispatcherInitializer.registerEventHandler(subscriber, className);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (InstantiationException e){
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }
}
