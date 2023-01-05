package nebulosa.indi.devices.mounts

import nebulosa.indi.devices.PropertyChangedEvent

data class MountPierSideChanged(override val device: Mount) : MountEvent, PropertyChangedEvent
