package nebulosa.indi.devices.mounts

import nebulosa.indi.devices.PropertyChangedEvent

data class MountTrackModesChanged(override val device: Mount) : MountEvent, PropertyChangedEvent
