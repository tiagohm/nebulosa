package nebulosa.indi.devices.mounts

import nebulosa.indi.devices.PropertyChangedEvent

data class MountTrackModeChanged(override val device: Mount) : MountEvent, PropertyChangedEvent
