package nebulosa.indi.device.cameras

import nebulosa.indi.device.PropertyChangedEvent

data class CameraCanSubFrameChanged(override val device: Camera) : CameraEvent, PropertyChangedEvent
