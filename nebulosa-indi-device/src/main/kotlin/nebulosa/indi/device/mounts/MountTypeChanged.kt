package nebulosa.indi.device.mounts

import nebulosa.indi.device.PropertyChangedEvent

data class MountTypeChanged(override val device: Mount) : MountEvent, PropertyChangedEvent
