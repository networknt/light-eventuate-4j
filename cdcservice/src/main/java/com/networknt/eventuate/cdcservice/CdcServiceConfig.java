package com.networknt.eventuate.cdcservice;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * CdcService Configuration class which load the config parameters from cdsservice.yml file
 */
public class CdcServiceConfig {

    String dbType;


    String dbHost;
    int dbPort;
    String dbUser;
    String dbPass;
    String dbName;
    String kafka;



    String zookeeper;
    String cdcDbUser;
    String cdcDbPass;

    //postgreSql config attributes
    String postgresDbHost;
    int postgresDBPort;
    String postgresDbUser;
    String postgresDbPass;
    String postgresDbName;



    @JsonIgnore
    String description;

    public CdcServiceConfig() {
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

    public String getDescription() {
        return description;
    }

    public String getPostgresDbHost() {
        return postgresDbHost;
    }

    public void setPostgresDbHost(String postgresDbHost) {
        this.postgresDbHost = postgresDbHost;
    }

    public int getPostgresDBPort() {
        return postgresDBPort;
    }

    public void setPostgresDBPort(int postgresDBPort) {
        this.postgresDBPort = postgresDBPort;
    }

    public String getPostgresDbUser() {
        return postgresDbUser;
    }

    public void setPostgresDbUser(String postgresDbUser) {
        this.postgresDbUser = postgresDbUser;
    }

    public String getPostgresDbPass() {
        return postgresDbPass;
    }

    public void setPostgresDbPass(String postgresDbPass) {
        this.postgresDbPass = postgresDbPass;
    }

    public String getPostgresDbName() {
        return postgresDbName;
    }

    public void setPostgresDbName(String postgresDbName) {
        this.postgresDbName = postgresDbName;
    }

    public void setDescription(String description) {
        this.description = description;
    }



}
