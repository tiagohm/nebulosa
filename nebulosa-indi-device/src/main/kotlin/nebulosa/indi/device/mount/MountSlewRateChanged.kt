package nebulosa.indi.device.mount

import nebulosa.indi.device.PropertyChangedEvent

data class MountSlewRateChanged(override val device: Mount) : MountEvent, PropertyChangedEvent
