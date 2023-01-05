package nebulosa.indi.devices.mounts

import nebulosa.indi.devices.PropertyChangedEvent

data class MountSlewingChanged(override val device: Mount) : MountEvent, PropertyChangedEvent
