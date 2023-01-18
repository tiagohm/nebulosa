package nebulosa.indi.device.mounts

import nebulosa.indi.device.PropertyChangedEvent

data class MountParkChanged(override val device: Mount) : MountEvent, PropertyChangedEvent
