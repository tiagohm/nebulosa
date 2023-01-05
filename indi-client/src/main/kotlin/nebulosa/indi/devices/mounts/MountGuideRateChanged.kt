package nebulosa.indi.devices.mounts

import nebulosa.indi.devices.PropertyChangedEvent

data class MountGuideRateChanged(override val device: Mount) : MountEvent, PropertyChangedEvent
