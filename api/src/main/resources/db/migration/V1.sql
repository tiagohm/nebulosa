CREATE TABLE IF NOT EXISTS preferences(
    key TEXT PRIMARY KEY,
    value TEXT
);

CREATE TABLE IF NOT EXISTS satellites(
    id INT8 PRIMARY KEY,
    name TEXT,
    tle TEXT,
    group_type INT8
);

CREATE TABLE IF NOT EXISTS stars(
    id INT8 PRIMARY KEY,
    name TEXT,
    magnitude REAL,
    right_ascension REAL,
    declination REAL,
    type INT1,
    sp_type TEXT,
    pm_ra REAL,
    pm_dec REAL,
    parallax REAL,
    radial_velocity REAL,
    redshift REAL,
    distance REAL,
    constellation INT1
);

CREATE INDEX IF NOT EXISTS stars_magnitude_idx ON stars(magnitude);
CREATE INDEX IF NOT EXISTS stars_type_idx ON stars(type);
CREATE INDEX IF NOT EXISTS stars_constellation_idx ON stars(constellation);

CREATE TABLE IF NOT EXISTS dsos(
    id INT8 PRIMARY KEY,
    name TEXT,
    magnitude REAL,
    right_ascension REAL,
    declination REAL,
    type INT1,
    major_axis REAL,
    minor_axis REAL,
    orientation REAL,
    pm_ra REAL,
    pm_dec REAL,
    radial_velocity REAL,
    parallax REAL,
    redshift REAL,
    distance REAL,
    constellation INT1
);

CREATE INDEX IF NOT EXISTS dsos_magnitude_idx ON dsos(magnitude);
CREATE INDEX IF NOT EXISTS dsos_type_idx ON dsos(type);
CREATE INDEX IF NOT EXISTS dsos_constellation_idx ON dsos(constellation);

CREATE TABLE IF NOT EXISTS locations(
    id INT8 PRIMARY KEY,
    name TEXT,
    longitude REAL,
    latitude REAL,
    elevation REAL,
    offset_in_minutes INT2,
    selected INT1
);
