package nebulosa.indi.devices.mounts

import nebulosa.indi.devices.PropertyChangedEvent

data class MountEquatorialCoordinatesChanged(override val device: Mount) : MountEvent, PropertyChangedEvent
