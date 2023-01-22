package nebulosa.indi.device.gps

import nebulosa.indi.device.PropertyChangedEvent

data class GPSCoordinateChanged(override val device: GPS) : GPSEvent<GPS>, PropertyChangedEvent
