package nebulosa.indi.device.cameras

import nebulosa.indi.device.PropertyChangedEvent

data class CameraCanAbortChanged(override val device: Camera) : CameraEvent, PropertyChangedEvent
