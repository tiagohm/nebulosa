package nebulosa.indi.devices.mounts

import nebulosa.indi.devices.PropertyChangedEvent
import nebulosa.indi.devices.gps.GPSEvent

data class MountTimeChanged(override val device: Mount) : MountEvent, GPSEvent<Mount>, PropertyChangedEvent
