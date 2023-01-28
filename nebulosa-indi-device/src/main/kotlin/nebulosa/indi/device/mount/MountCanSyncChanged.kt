package nebulosa.indi.device.mount

import nebulosa.indi.device.PropertyChangedEvent

data class MountCanSyncChanged(override val device: Mount) : MountEvent, PropertyChangedEvent
