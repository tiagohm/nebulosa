package nebulosa.indi.device.cameras

import nebulosa.indi.device.PropertyChangedEvent

data class CameraCanBinChanged(override val device: Camera) : CameraEvent, PropertyChangedEvent
