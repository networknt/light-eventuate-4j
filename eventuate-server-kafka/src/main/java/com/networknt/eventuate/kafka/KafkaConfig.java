package com.networknt.eventuate.kafka;

import com.fasterxml.jackson.annotation.JsonIgnore;


/**
 * A Kafka setting configuration file. It get from defined resource yml file
 *
 */
public class KafkaConfig {
    private String acks;
    private int retries;
    private int batchSize;
    private int lingerms;
    private long bufferMemory;
    private String keySerializer;
    private String valueSerializer;
    private boolean enableaAutocommit;
    private int sessionTimeout;
    private String autoOffsetreset;
    private String bootstrapServers;
    private String keyDeSerializer;
    private String valueDeSerializer;


    @JsonIgnore
    String description;


    public KafkaConfig() {
    }

    public String getAcks() {
        return acks;
    }

    public void setAcks(String acks) {
        this.acks = acks;
    }

    public int getRetries() {
        return retries;
    }

    public void setRetries(int retries) {
        this.retries = retries;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public int getLingerms() {
        return lingerms;
    }

    public void setLingerms(int lingerms) {
        this.lingerms = lingerms;
    }

    public long getBufferMemory() {
        return bufferMemory;
    }

    public void setBufferMemory(long bufferMemory) {
        this.bufferMemory = bufferMemory;
    }

    public String getKeySerializer() {
        return keySerializer;
    }

    public void setKeySerializer(String keySerializer) {
        this.keySerializer = keySerializer;
    }

    public String getValueSerializer() {
        return valueSerializer;
    }

    public void setValueSerializer(String valueSerializer) {
        this.valueSerializer = valueSerializer;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isEnableaAutocommit() {
        return enableaAutocommit;
    }

    public void setEnableaAutocommit(boolean enableaAutocommit) {
        this.enableaAutocommit = enableaAutocommit;
    }

    public int getSessionTimeout() {
        return sessionTimeout;
    }

    public void setSessionTimeout(int sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
    }

    public String getAutoOffsetreset() {
        return autoOffsetreset;
    }

    public void setAutoOffsetreset(String autoOffsetreset) {
        this.autoOffsetreset = autoOffsetreset;
    }

    public String getBootstrapServers() {
        return bootstrapServers;
    }

    public void setBootstrapServers(String bootstrapServers) {
        this.bootstrapServers = bootstrapServers;
    }

    public String getKeyDeSerializer() {
        return keyDeSerializer;
    }

    public void setKeyDeSerializer(String keyDeSerializer) {
        this.keyDeSerializer = keyDeSerializer;
    }

    public String getValueDeSerializer() {
        return valueDeSerializer;
    }

    public void setValueDeSerializer(String valueDeSerializer) {
        this.valueDeSerializer = valueDeSerializer;
    }

}
