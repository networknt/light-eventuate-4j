
CREATE TABLESPACE tbs_perm_01
  DATAFILE 'tbs_perm_01.dat'
    SIZE 10M
    REUSE
    AUTOEXTEND ON NEXT 10M MAXSIZE 200M;

CREATE USER EVENTUATE
  IDENTIFIED BY password
  DEFAULT TABLESPACE tbs_perm_01
  QUOTA 20M on tbs_perm_01;

--DROP table EVENTUATE.events;
--DROP table EVENTUATE.entities;
--DROP table EVENTUATE.snapshots;

create table EVENTUATE.events (
  event_id varchar(1000) PRIMARY KEY,
  event_type varchar(1000),
  event_data varchar(1000) NOT NULL,
  entity_type VARCHAR(1000) NOT NULL,
  entity_id VARCHAR(1000) NOT NULL,
  triggering_event VARCHAR(1000),
  metadata VARCHAR(1000),
  published INT DEFAULT 0
);

CREATE INDEX EVENTUATE.events_idx ON EVENTUATE.events(entity_type, entity_id, event_id);
CREATE INDEX EVENTUATE.events_published_idx ON EVENTUATE.events(published, event_id);

create table EVENTUATE.entities (
  entity_type VARCHAR(1000),
  entity_id VARCHAR(1000),
  entity_version VARCHAR(1000) NOT NULL,
  PRIMARY KEY(entity_type, entity_id)
);


create table EVENTUATE.snapshots (
  entity_type VARCHAR(1000),
  entity_id VARCHAR(1000),
  entity_version VARCHAR(1000),
  snapshot_type VARCHAR(1000) NOT NULL,
  snapshot_json VARCHAR(1000) NOT NULL,
  triggering_events VARCHAR(1000),
  PRIMARY KEY(entity_type, entity_id, entity_version)
);
