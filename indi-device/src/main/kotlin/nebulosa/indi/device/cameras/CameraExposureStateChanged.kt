package nebulosa.indi.device.cameras

import nebulosa.indi.device.PropertyChangedEvent
import nebulosa.indi.protocol.PropertyState

data class CameraExposureStateChanged(
    override val device: Camera,
    val previousState: PropertyState,
) : CameraEvent, PropertyChangedEvent
