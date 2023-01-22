package nebulosa.indi.device.mounts

import nebulosa.indi.device.PropertyChangedEvent

data class MountSlewingChanged(override val device: Mount) : MountEvent, PropertyChangedEvent
