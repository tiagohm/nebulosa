package nebulosa.indi.device.gps

import nebulosa.indi.device.PropertyChangedEvent

data class GPSTimeChanged(override val device: GPS) : GPSEvent<GPS>, PropertyChangedEvent
