package nebulosa.indi.devices.mounts

import nebulosa.indi.devices.PropertyChangedEvent

data class MountCanParkChanged(override val device: Mount) : MountEvent, PropertyChangedEvent
