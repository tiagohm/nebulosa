package nebulosa.indi.devices.mounts

import nebulosa.indi.devices.PropertyChangedEvent

data class MountTrackingChanged(override val device: Mount) : MountEvent, PropertyChangedEvent
