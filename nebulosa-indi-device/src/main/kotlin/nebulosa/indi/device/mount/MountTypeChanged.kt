package nebulosa.indi.device.mount

import nebulosa.indi.device.PropertyChangedEvent

data class MountTypeChanged(override val device: Mount) : MountEvent, PropertyChangedEvent
