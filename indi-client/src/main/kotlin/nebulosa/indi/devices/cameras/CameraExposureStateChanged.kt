package nebulosa.indi.devices.cameras

import nebulosa.indi.devices.PropertyChangedEvent
import nebulosa.indi.protocol.PropertyState

data class CameraExposureStateChanged(
    override val device: Camera,
    val previousState: PropertyState,
) : CameraEvent, PropertyChangedEvent
