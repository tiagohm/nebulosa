package nebulosa.indi.device.mount

import nebulosa.indi.device.PropertyChangedEvent

data class MountCanParkChanged(override val device: Mount) : MountEvent, PropertyChangedEvent
