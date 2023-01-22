package nebulosa.indi.device.mounts

import nebulosa.indi.device.PropertyChangedEvent

data class MountEquatorialCoordinatesChanged(override val device: Mount) : MountEvent, PropertyChangedEvent
