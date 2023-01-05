package nebulosa.indi.devices.mounts

import nebulosa.indi.devices.PropertyChangedEvent

data class MountTypeChanged(override val device: Mount) : MountEvent, PropertyChangedEvent
