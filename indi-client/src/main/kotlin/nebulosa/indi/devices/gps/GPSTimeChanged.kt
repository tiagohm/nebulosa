package nebulosa.indi.devices.gps

import nebulosa.indi.devices.PropertyChangedEvent

data class GPSTimeChanged(override val device: GPS) : GPSEvent<GPS>, PropertyChangedEvent
