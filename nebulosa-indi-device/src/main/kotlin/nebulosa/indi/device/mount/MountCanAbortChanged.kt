package nebulosa.indi.device.mount

import nebulosa.indi.device.PropertyChangedEvent

data class MountCanAbortChanged(override val device: Mount) : MountEvent, PropertyChangedEvent
