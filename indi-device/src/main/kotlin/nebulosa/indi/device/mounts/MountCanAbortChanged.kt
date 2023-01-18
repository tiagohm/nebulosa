package nebulosa.indi.device.mounts

import nebulosa.indi.device.PropertyChangedEvent

data class MountCanAbortChanged(override val device: Mount) : MountEvent, PropertyChangedEvent
