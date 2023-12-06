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
    gain REAL,
    path TEXT,
    enabled INT1
);

CREATE INDEX IF NOT EXISTS calibration_frames_type_idx ON calibration_frames(type);
CREATE INDEX IF NOT EXISTS calibration_frames_camera_idx ON calibration_frames(camera);
CREATE INDEX IF NOT EXISTS calibration_frames_filter_idx ON calibration_frames(filter);
CREATE INDEX IF NOT EXISTS calibration_frames_width_idx ON calibration_frames(width);
CREATE INDEX IF NOT EXISTS calibration_frames_height_idx ON calibration_frames(height);
CREATE INDEX IF NOT EXISTS calibration_frames_exposure_time_idx ON calibration_frames(exposure_time);
CREATE INDEX IF NOT EXISTS calibration_frames_bin_x_idx ON calibration_frames(bin_x);
CREATE INDEX IF NOT EXISTS calibration_frames_bin_y_idx ON calibration_frames(bin_y);
CREATE INDEX IF NOT EXISTS calibration_frames_gain_idx ON calibration_frames(gain);
CREATE INDEX IF NOT EXISTS calibration_frames_enabled_idx ON calibration_frames(enabled);
