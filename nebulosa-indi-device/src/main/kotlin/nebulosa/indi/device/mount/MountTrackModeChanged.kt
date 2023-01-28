package nebulosa.indi.device.mount

import nebulosa.indi.device.PropertyChangedEvent

data class MountTrackModeChanged(override val device: Mount) : MountEvent, PropertyChangedEvent
