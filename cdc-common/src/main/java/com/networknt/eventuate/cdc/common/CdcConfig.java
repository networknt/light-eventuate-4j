package com.networknt.eventuate.cdc.common;

public class CdcConfig {
    String dbType;
    String jdbcUrl;
    String dbHost;
    int dbPort;
    String dbUser;
    String dbPass;
    String dbName;
    String dbHistoryTopicName;
    String sourceTableName;
    int maximumPoolSize;
    String kafka;
    String zookeeper;
    String cdcDbUser;
    String cdcDbPass;
    long binlogClientId;

    public CdcConfig() {
        binlogClientId = System.currentTimeMillis();
    }

    public String getDbType() {
        return dbType;
    }

    public void setDbType(String dbType) {
        this.dbType = dbType;
    }

    public String getDbHost() {
        return dbHost;
    }

    public void setDbHost(String dbHost) {
        this.dbHost = dbHost;
    }

    public int getDbPort() {
        return dbPort;
    }

    public void setDbPort(int dbPort) {
        this.dbPort = dbPort;
    }

    public String getDbUser() {
        return dbUser;
    }

    public void setDbUser(String dbUser) {
        this.dbUser = dbUser;
    }

    public String getDbPass() {
        return dbPass;
    }

    public void setDbPass(String dbPass) {
        this.dbPass = dbPass;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getKafka() {
        return kafka;
    }

    public void setKafka(String kafka) {
        this.kafka = kafka;
    }

    public String getZookeeper() {
        return zookeeper;
    }

    public void setZookeeper(String zookeeper) {
        this.zookeeper = zookeeper;
    }

    public String getCdcDbUser() {
        return cdcDbUser;
    }

    public void setCdcDbUser(String cdcDbUser) {
        this.cdcDbUser = cdcDbUser;
    }

    public String getCdcDbPass() {
        return cdcDbPass;
    }

    public void setCdcDbPass(String cdcDbPass) {
        this.cdcDbPass = cdcDbPass;
    }

    public String getDbHistoryTopicName() {
        return dbHistoryTopicName;
    }

    public void setDbHistoryTopicName(String dbHistoryTopicName) {
        this.dbHistoryTopicName = dbHistoryTopicName;
    }

    public String getSourceTableName() {
        return sourceTableName;
    }

    public void setSourceTableName(String sourceTableName) {
        this.sourceTableName = sourceTableName;
    }

    public long getBinlogClientId() { return binlogClientId; }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public void setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    public int getMaximumPoolSize() {
        return maximumPoolSize;
    }

    public void setMaximumPoolSize(int maximumPoolSize) {
        this.maximumPoolSize = maximumPoolSize;
    }
}
