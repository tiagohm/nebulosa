package nebulosa.indi.devices.gps

data class GPSAttached(override val device: GPS) : GPSEvent<GPS>
