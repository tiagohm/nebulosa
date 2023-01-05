package nebulosa.indi.devices.mounts

import nebulosa.indi.devices.PropertyChangedEvent

data class MountCanAbortChanged(override val device: Mount) : MountEvent, PropertyChangedEvent
