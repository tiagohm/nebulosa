package nebulosa.indi.device.camera

import nebulosa.indi.device.PropertyChangedEvent

data class CameraCanSubFrameChanged(override val device: Camera) : CameraEvent, PropertyChangedEvent
