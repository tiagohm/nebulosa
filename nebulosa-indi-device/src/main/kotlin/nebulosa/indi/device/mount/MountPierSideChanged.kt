package nebulosa.indi.device.mount

import nebulosa.indi.device.PropertyChangedEvent

data class MountPierSideChanged(override val device: Mount) : MountEvent, PropertyChangedEvent
