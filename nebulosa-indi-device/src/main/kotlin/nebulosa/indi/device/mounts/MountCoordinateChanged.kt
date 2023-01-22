package nebulosa.indi.device.mounts

import nebulosa.indi.device.PropertyChangedEvent

data class MountCoordinateChanged(override val device: Mount) : MountEvent, PropertyChangedEvent
