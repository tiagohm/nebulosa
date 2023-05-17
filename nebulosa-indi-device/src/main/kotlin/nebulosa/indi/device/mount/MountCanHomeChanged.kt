package nebulosa.indi.device.mount

import nebulosa.indi.device.PropertyChangedEvent

data class MountCanHomeChanged(override val device: Mount) : MountEvent, PropertyChangedEvent
