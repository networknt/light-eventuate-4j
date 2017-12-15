package com.networknt.eventuate.server.common;

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
    long binlogClientId;
    String mySqlBinLogClientName;
    String leadershipLockPath;
    String oldDbHistoryTopicName;

    int maxEventsPerPolling;
    int maxAttemptsForPolling;
    int pollingRetryIntervalInMilliseconds;
    int pollingIntervalInMilliseconds;

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

    public String getMySqlBinLogClientName() {
        return mySqlBinLogClientName;
    }

    public void setMySqlBinLogClientName(String mySqlBinLogClientName) {
        this.mySqlBinLogClientName = mySqlBinLogClientName;
    }

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

    public void setBinlogClientId(long binlogClientId) {
        this.binlogClientId = binlogClientId;
    }

    public String getLeadershipLockPath() {
        return leadershipLockPath;
    }

    public void setLeadershipLockPath(String leadershipLockPath) {
        this.leadershipLockPath = leadershipLockPath;
    }

    public String getOldDbHistoryTopicName() {
        return oldDbHistoryTopicName;
    }

    public void setOldDbHistoryTopicName(String oldDbHistoryTopicName) {
        this.oldDbHistoryTopicName = oldDbHistoryTopicName;
    }

    public int getMaxEventsPerPolling() {
        return maxEventsPerPolling;
    }

    public void setMaxEventsPerPolling(int maxEventsPerPolling) {
        this.maxEventsPerPolling = maxEventsPerPolling;
    }

    public int getMaxAttemptsForPolling() {
        return maxAttemptsForPolling;
    }

    public void setMaxAttemptsForPolling(int maxAttemptsForPolling) {
        this.maxAttemptsForPolling = maxAttemptsForPolling;
    }

    public int getPollingRetryIntervalInMilliseconds() {
        return pollingRetryIntervalInMilliseconds;
    }

    public void setPollingRetryIntervalInMilliseconds(int pollingRetryIntervalInMilliseconds) {
        this.pollingRetryIntervalInMilliseconds = pollingRetryIntervalInMilliseconds;
    }

    public int getPollingIntervalInMilliseconds() {
        return pollingIntervalInMilliseconds;
    }

    public void setPollingIntervalInMilliseconds(int pollingIntervalInMilliseconds) {
        this.pollingIntervalInMilliseconds = pollingIntervalInMilliseconds;
    }
}
