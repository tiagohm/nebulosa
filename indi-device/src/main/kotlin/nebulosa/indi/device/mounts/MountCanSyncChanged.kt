package nebulosa.indi.device.mounts

import nebulosa.indi.device.PropertyChangedEvent

data class MountCanSyncChanged(override val device: Mount) : MountEvent, PropertyChangedEvent
