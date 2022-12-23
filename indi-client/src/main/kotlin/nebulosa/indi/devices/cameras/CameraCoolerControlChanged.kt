package nebulosa.indi.devices.cameras

import nebulosa.indi.devices.PropertyChangedEvent

data class CameraCoolerControlChanged(override val device: Camera) : CameraEvent, PropertyChangedEvent
