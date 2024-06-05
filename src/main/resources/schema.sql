DROP ALL OBJECTS;

CREATE TABLE IF NOT EXISTS USERS (
  ID        BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
  NAME      VARCHAR(64) NOT NULL,
  EMAIL     VARCHAR(64) NOT NULL,
  CONSTRAINT PK_USER PRIMARY KEY (ID),
  CONSTRAINT UQ_USER_EMAIL UNIQUE (EMAIL)
);

CREATE TABLE IF NOT EXISTS ITEMS (
  ID            BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
  NAME          VARCHAR(32) NOT NULL,
  DESCRIPTION   VARCHAR(256) NOT NULL,
  AVAILABLE     BOOLEAN,
  OWNER         BIGINT NOT NULL REFERENCES USERS (ID) ON DELETE CASCADE,
  CONSTRAINT PK_ITEM PRIMARY KEY (ID)
);

CREATE TABLE IF NOT EXISTS BOOKINGS (
  ID            BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
  START_TIME    TIMESTAMP NOT NULL,
  END_TIME      TIMESTAMP NOT NULL,
  ITEM_ID       BIGINT NOT NULL REFERENCES ITEMS (ID),
  BOOKER_ID     BIGINT NOT NULL REFERENCES USERS (ID),
  STATUS        VARCHAR(10) NOT NULL,
  CONSTRAINT PK_BOOKING PRIMARY KEY (ID)
);

CREATE TABLE IF NOT EXISTS COMMENTS (
    ID          BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    COMMENT     VARCHAR(2000) NOT NULL,
    ITEM_ID     BIGINT NOT NULL REFERENCES ITEMS (ID),
    USER_ID     BIGINT NOT NULL REFERENCES USERS (ID),
    CREATED     TIMESTAMP NOT NULL,
    CONSTRAINT PK_COMMENTS PRIMARY KEY (ID)
);