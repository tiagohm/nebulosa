package nebulosa.indi.device.gps

import nebulosa.indi.device.DeviceDetached

data class GPSDetached(override val device: GPS) : GPSEvent<GPS>, DeviceDetached<GPS>
