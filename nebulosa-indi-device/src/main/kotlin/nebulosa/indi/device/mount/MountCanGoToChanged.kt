package nebulosa.indi.device.mount

import nebulosa.indi.device.PropertyChangedEvent

data class MountCanGoToChanged(override val device: Mount) : MountEvent, PropertyChangedEvent
