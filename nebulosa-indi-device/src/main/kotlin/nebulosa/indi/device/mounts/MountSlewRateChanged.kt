package nebulosa.indi.device.mounts

import nebulosa.indi.device.PropertyChangedEvent

data class MountSlewRateChanged(override val device: Mount) : MountEvent, PropertyChangedEvent
