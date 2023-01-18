package nebulosa.indi.device.gps

data class GPSAttached(override val device: GPS) : GPSEvent<GPS>
