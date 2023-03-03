package nebulosa.indi.device.mount

import nebulosa.indi.device.PropertyChangedEvent

data class MountEquatorialJ2000CoordinatesChanged(override val device: Mount) : MountEvent, PropertyChangedEvent
