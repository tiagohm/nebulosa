package nebulosa.indi.devices.mounts

import nebulosa.indi.devices.PropertyChangedEvent

data class MountCoordinateChanged(override val device: Mount) : MountEvent, PropertyChangedEvent
