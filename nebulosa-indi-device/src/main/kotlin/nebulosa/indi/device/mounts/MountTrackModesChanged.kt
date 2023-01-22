package nebulosa.indi.device.mounts

import nebulosa.indi.device.PropertyChangedEvent

data class MountTrackModesChanged(override val device: Mount) : MountEvent, PropertyChangedEvent
