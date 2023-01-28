package nebulosa.indi.device.mounts

import nebulosa.indi.device.PropertyChangedEvent

data class MountGeographicCoordinateChanged(override val device: Mount) : MountEvent, PropertyChangedEvent
