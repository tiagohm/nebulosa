package nebulosa.indi.device.mount

import nebulosa.indi.device.PropertyChangedEvent

data class MountHorizontalCoordinatesChanged(override val device: Mount) : MountEvent, PropertyChangedEvent
