package nebulosa.indi.devices.mounts

import nebulosa.indi.devices.PropertyChangedEvent

data class MountParkChanged(override val device: Mount) : MountEvent, PropertyChangedEvent
