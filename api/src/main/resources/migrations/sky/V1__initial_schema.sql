-- NOTE: Why UpperCase? https://www.h2database.com/html/grammar.html?highlight=DATABASE_TO_UPPER&search=DATABASE_TO_UPPER#firstFound

CREATE TABLE IF NOT EXISTS SATELLITES(
    ID BIGINT NOT NULL PRIMARY KEY,
    NAME VARCHAR_IGNORECASE(256) NOT NULL,
    TLE VARCHAR(4096) NOT NULL,
    GROUPS TINYINT ARRAY NOT NULL
);

CREATE INDEX IF NOT EXISTS IDX_SATELLITES_NAME ON SATELLITES(NAME);

CREATE TABLE IF NOT EXISTS SKY_OBJECTS(
    ID IDENTITY NOT NULL PRIMARY KEY,
    NAME VARCHAR_IGNORECASE(256) NOT NULL,
    TYPE SMALLINT NOT NULL,
    RIGHT_ASCENSION DOUBLE PRECISION NOT NULL,
    DECLINATION DOUBLE PRECISION NOT NULL,
    MAGNITUDE DOUBLE PRECISION NOT NULL,
    PM_RA DOUBLE PRECISION NOT NULL,
    PM_DEC DOUBLE PRECISION NOT NULL,
    PARALLAX DOUBLE PRECISION NOT NULL,
    RADIAL_VEL DOUBLE PRECISION NOT NULL,
    REDSHIFT DOUBLE PRECISION NOT NULL,
    CONSTELLATION TINYINT NOT NULL
);

CREATE INDEX IF NOT EXISTS IDX_SKY_OBJECTS_MAGNITUDE ON SKY_OBJECTS(MAGNITUDE);
CREATE INDEX IF NOT EXISTS IDX_SKY_OBJECTS_RIGHT_ASCENSION ON SKY_OBJECTS(RIGHT_ASCENSION);
CREATE INDEX IF NOT EXISTS IDX_SKY_OBJECTS_DECLINATION ON SKY_OBJECTS(DECLINATION);
CREATE INDEX IF NOT EXISTS IDX_SKY_OBJECTS_TYPE ON SKY_OBJECTS(TYPE);
CREATE INDEX IF NOT EXISTS IDX_SKY_OBJECTS_CONSTELLATION ON SKY_OBJECTS(CONSTELLATION);
CREATE INDEX IF NOT EXISTS IDX_SKY_OBJECTS_NAME ON SKY_OBJECTS(NAME);