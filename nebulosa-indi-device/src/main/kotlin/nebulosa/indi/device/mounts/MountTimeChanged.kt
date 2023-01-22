package nebulosa.indi.device.mounts

import nebulosa.indi.device.PropertyChangedEvent
import nebulosa.indi.device.gps.GPSEvent

data class MountTimeChanged(override val device: Mount) : MountEvent, GPSEvent<Mount>, PropertyChangedEvent
