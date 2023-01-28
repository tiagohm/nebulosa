package nebulosa.indi.device.mount

import nebulosa.indi.device.PropertyChangedEvent

data class MountSlewingChanged(override val device: Mount) : MountEvent, PropertyChangedEvent
