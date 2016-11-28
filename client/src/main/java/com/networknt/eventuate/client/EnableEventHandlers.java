package com.networknt.eventuate.client;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Configures event handling Spring beans
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
//@Import(EventuateJavaClientDomainConfiguration.class)
public @interface EnableEventHandlers {
}
