CREATE TABLE IF NOT EXISTS configs(
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
    offset_in_minutes INT2
);

CREATE TABLE IF NOT EXISTS guide_calibrations(
    id INT8 PRIMARY KEY,
    camera TEXT,
    mount TEXT,
    guide_output TEXT,
    saved_at INT8,
    x_rate REAL,
    y_rate REAL,
    x_angle REAL,
    y_angle REAL,
    declination REAL,
    rotator_angle REAL,
    binning INT1,
    pier_side_at_east INT1,
    ra_guide_parity INT1,
    dec_guide_parity INT1
);

CREATE INDEX IF NOT EXISTS guide_calibrations_camera_idx ON guide_calibrations(camera);
CREATE INDEX IF NOT EXISTS guide_calibrations_mount_idx ON guide_calibrations(mount);
CREATE INDEX IF NOT EXISTS guide_calibrations_guide_output_idx ON guide_calibrations(guide_output);
CREATE INDEX IF NOT EXISTS guide_calibrations_saved_at_idx ON guide_calibrations(saved_at);
