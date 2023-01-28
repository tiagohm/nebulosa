package nebulosa.indi.device.mount

import nebulosa.indi.device.PropertyChangedEvent

data class MountTrackModesChanged(override val device: Mount) : MountEvent, PropertyChangedEvent
