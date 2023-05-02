CREATE INDEX IF NOT EXISTS idx_dsos_constellation ON dsos(constellation);
CREATE INDEX IF NOT EXISTS idx_dsos_type ON dsos(type);
CREATE INDEX IF NOT EXISTS idx_dsos_magnitude ON dsos(magnitude);
CREATE INDEX IF NOT EXISTS idx_dsos_names ON dsos(names);

CREATE INDEX IF NOT EXISTS idx_stars_constellation ON stars(constellation);
CREATE INDEX IF NOT EXISTS idx_stars_type ON stars(type);
CREATE INDEX IF NOT EXISTS idx_stars_magnitude ON stars(magnitude);
CREATE INDEX IF NOT EXISTS idx_stars_names ON stars(names);
