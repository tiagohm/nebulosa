package nebulosa.indi.device.mount

import nebulosa.indi.device.PropertyChangedEvent

data class MountTrackingChanged(override val device: Mount) : MountEvent, PropertyChangedEvent
