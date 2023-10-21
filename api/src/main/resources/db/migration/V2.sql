CREATE TABLE IF NOT EXISTS calibration_frames(
    id INT8 PRIMARY KEY,
    type INT1,
    camera TEXT,
    filter TEXT,
    exposure_time INT8,
    temperature REAL,
    width INT4,
    height INT4,
    bin_x INT1,
    bin_y INT1,
    path TEXT,
    enabled INT1
)

CREATE INDEX IF NOT EXISTS calibration_frames_type_idx ON calibration_frames(type);
CREATE INDEX IF NOT EXISTS calibration_frames_camera_idx ON calibration_frames(camera);
CREATE INDEX IF NOT EXISTS calibration_frames_filter_idx ON calibration_frames(filter);
