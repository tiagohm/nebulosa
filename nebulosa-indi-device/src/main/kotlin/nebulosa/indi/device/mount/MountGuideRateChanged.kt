package nebulosa.indi.device.mount

import nebulosa.indi.device.PropertyChangedEvent

data class MountGuideRateChanged(override val device: Mount) : MountEvent, PropertyChangedEvent
