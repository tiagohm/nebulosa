package nebulosa.indi.device.camera

import nebulosa.indi.device.PropertyChangedEvent

data class CameraPixelSizeChanged(override val device: Camera) : CameraEvent, PropertyChangedEvent
