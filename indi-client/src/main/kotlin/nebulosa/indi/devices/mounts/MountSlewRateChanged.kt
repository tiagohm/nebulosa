package nebulosa.indi.devices.mounts

import nebulosa.indi.devices.PropertyChangedEvent

data class MountSlewRateChanged(override val device: Mount) : MountEvent, PropertyChangedEvent
