CREATE TABLE EMAILDATA_CHANNEL_MESSAGE (
   MESSAGE_ID VARCHAR(36) NOT NULL,
   GROUP_KEY VARCHAR(255) NOT NULL,
   CREATED_DATE BIGINT NOT NULL,
   MESSAGE_BYTES BYTEA NOT NULL,
   REGION VARCHAR(100),
   MESSAGE_PRIORITY INTEGER,
   MESSAGE_SEQUENCE BIGINT,
   PRIMARY KEY (MESSAGE_ID, REGION)
);

CREATE INDEX EMAILDATA_GROUP_KEY_INDEX
    ON EMAILDATA_CHANNEL_MESSAGE (GROUP_KEY);

CREATE TABLE BANKDATA_CHANNEL_MESSAGE (
  MESSAGE_ID VARCHAR(36) NOT NULL,
  GROUP_KEY VARCHAR(255) NOT NULL,
  CREATED_DATE BIGINT NOT NULL,
  MESSAGE_BYTES BYTEA NOT NULL,
  REGION VARCHAR(100),
  MESSAGE_PRIORITY INTEGER,
  MESSAGE_SEQUENCE BIGINT,
  PRIMARY KEY (MESSAGE_ID, REGION)
);

CREATE INDEX BANKDATA_GROUP_KEY_INDEX
    ON BANKDATA_CHANNEL_MESSAGE (GROUP_KEY);

CREATE TABLE DELIVERYDATA_CHANNEL_MESSAGE (
  MESSAGE_ID VARCHAR(36) NOT NULL,
  GROUP_KEY VARCHAR(255) NOT NULL,
  CREATED_DATE BIGINT NOT NULL,
  MESSAGE_BYTES BYTEA NOT NULL,
  REGION VARCHAR(100),
  MESSAGE_PRIORITY INTEGER,
  MESSAGE_SEQUENCE BIGINT,
  PRIMARY KEY (MESSAGE_ID, REGION)
);

CREATE INDEX DELIVERYDATA_GROUP_KEY_INDEX
    ON DELIVERYDATA_CHANNEL_MESSAGE (GROUP_KEY);

CREATE TABLE BANKTOSTOREDATA_CHANNEL_MESSAGE (
     MESSAGE_ID VARCHAR(36) NOT NULL,
     GROUP_KEY VARCHAR(255) NOT NULL,
     CREATED_DATE BIGINT NOT NULL,
     MESSAGE_BYTES BYTEA NOT NULL,
     REGION VARCHAR(100),
     MESSAGE_PRIORITY INTEGER,
     MESSAGE_SEQUENCE BIGINT,
     PRIMARY KEY (MESSAGE_ID, REGION)
);

CREATE INDEX BANKTOSTOREDATA_GROUP_KEY_INDEX
    ON BANKTOSTOREDATA_CHANNEL_MESSAGE (GROUP_KEY);

CREATE TABLE DELIVERYTOSTOREDATA_CHANNEL_MESSAGE (
     MESSAGE_ID VARCHAR(36) NOT NULL,
     GROUP_KEY VARCHAR(255) NOT NULL,
     CREATED_DATE BIGINT NOT NULL,
     MESSAGE_BYTES BYTEA NOT NULL,
     REGION VARCHAR(100),
     MESSAGE_PRIORITY INTEGER,
     MESSAGE_SEQUENCE BIGINT,
     PRIMARY KEY (MESSAGE_ID, REGION)
);

CREATE INDEX DELIVERYTOSTOREDATA_GROUP_KEY_INDEX
    ON DELIVERYTOSTOREDATA_CHANNEL_MESSAGE (GROUP_KEY);