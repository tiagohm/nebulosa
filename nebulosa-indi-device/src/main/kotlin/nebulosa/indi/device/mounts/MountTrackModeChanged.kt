package nebulosa.indi.device.mounts

import nebulosa.indi.device.PropertyChangedEvent

data class MountTrackModeChanged(override val device: Mount) : MountEvent, PropertyChangedEvent
