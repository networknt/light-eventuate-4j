CREATE DATABASE eventuate;

USE eventuate;


DROP table IF EXISTS events;
DROP table IF EXISTS  entities;
DROP table IF EXISTS  snapshots;

create table events (
  event_id VARCHAR(256),
  event_type VARCHAR(256),
  event_data VARCHAR(20000),
  entity_type VARCHAR(256),
  entity_id VARCHAR(256),
  triggering_event VARCHAR(256),
   PRIMARY KEY(event_id)
);

create table entities (
  entity_type VARCHAR(256),
  entity_id VARCHAR(256),
  entity_version VARCHAR(256),
  create_dttm TIMESTAMP  DEFAULT NOW(),
  PRIMARY KEY(entity_type, entity_id)
);

create table snapshots (
  entity_type VARCHAR(256),
  entity_id VARCHAR(256),
  entity_version VARCHAR(256),
  snapshot_type VARCHAR(256),
  snapshot_json VARCHAR(20000),
  triggering_events VARCHAR(256),
  PRIMARY KEY(entity_type, entity_id,entity_version)
);

CREATE USER 'mysqluser' IDENTIFIED BY 'mysqlpw';

grant  ALL PRIVILEGES on eventuate  to 'mysqluser'@'localhost'


GRANT SELECT, RELOAD, SHOW DATABASES, REPLICATION SLAVE, REPLICATION CLIENT ON *.* TO 'mysqluser' IDENTIFIED BY 'mysqlpw';