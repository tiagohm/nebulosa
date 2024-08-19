package nebulosa.indi.device.gps

import nebulosa.indi.device.DeviceAttached

data class GPSAttached(override val device: GPS) : GPSEvent<GPS>, DeviceAttached<GPS>
