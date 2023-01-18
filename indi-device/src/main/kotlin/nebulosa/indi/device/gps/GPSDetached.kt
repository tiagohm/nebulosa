package nebulosa.indi.device.gps

data class GPSDetached(override val device: GPS) : GPSEvent<GPS>
