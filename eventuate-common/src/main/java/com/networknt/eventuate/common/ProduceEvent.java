package com.networknt.eventuate.common;

import java.util.concurrent.CompletableFuture;

/**
 * An interface for a produce event to message broker. In case user want handle event produce by themselves, implement interface to porduce event to message broker
 */
public interface ProduceEvent {

     CompletableFuture<?> send(String topic, String key, String body);
}
