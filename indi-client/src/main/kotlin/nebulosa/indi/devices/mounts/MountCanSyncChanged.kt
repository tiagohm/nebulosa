package nebulosa.indi.devices.mounts

import nebulosa.indi.devices.PropertyChangedEvent

data class MountCanSyncChanged(override val device: Mount) : MountEvent, PropertyChangedEvent
