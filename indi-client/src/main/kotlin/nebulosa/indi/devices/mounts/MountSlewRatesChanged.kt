package nebulosa.indi.devices.mounts

import nebulosa.indi.devices.PropertyChangedEvent

data class MountSlewRatesChanged(override val device: Mount) : MountEvent, PropertyChangedEvent
