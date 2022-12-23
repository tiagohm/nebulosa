package nebulosa.indi.devices.cameras

import nebulosa.indi.devices.PropertyChangedEvent

data class CameraCanSubFrameChanged(override val device: Camera) : CameraEvent, PropertyChangedEvent
