package nebulosa.indi.device.mounts

import nebulosa.indi.device.PropertyChangedEvent

data class MountPierSideChanged(override val device: Mount) : MountEvent, PropertyChangedEvent
