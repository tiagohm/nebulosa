package nebulosa.indi.device.mounts

import nebulosa.indi.device.PropertyChangedEvent

data class MountTrackingChanged(override val device: Mount) : MountEvent, PropertyChangedEvent
