package nebulosa.indi.devices.cameras

import nebulosa.indi.devices.PropertyChangedEvent

data class CameraCoolerChanged(override val device: Camera) : CameraEvent, PropertyChangedEvent
