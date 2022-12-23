package nebulosa.indi.devices.cameras

import nebulosa.indi.devices.PropertyChangedEvent

data class CameraCanBinChanged(override val device: Camera) : CameraEvent, PropertyChangedEvent
