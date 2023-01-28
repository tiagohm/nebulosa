package nebulosa.indi.device.camera

import nebulosa.indi.device.PropertyChangedEvent

data class CameraCanBinChanged(override val device: Camera) : CameraEvent, PropertyChangedEvent
