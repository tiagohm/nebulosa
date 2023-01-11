package nebulosa.indi.devices.mounts

import nebulosa.indi.devices.PropertyChangedEvent

data class GPSTimeChanged(override val device: GPS) : GPSEvent<GPS>, PropertyChangedEvent
