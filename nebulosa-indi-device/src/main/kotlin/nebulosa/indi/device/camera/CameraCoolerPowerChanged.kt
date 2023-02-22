package nebulosa.indi.device.camera

import nebulosa.indi.device.PropertyChangedEvent

data class CameraCoolerPowerChanged(override val device: Camera) : CameraEvent, PropertyChangedEvent
