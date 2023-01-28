package nebulosa.indi.device.camera

import nebulosa.indi.device.PropertyChangedEvent

data class CameraCanAbortChanged(override val device: Camera) : CameraEvent, PropertyChangedEvent
