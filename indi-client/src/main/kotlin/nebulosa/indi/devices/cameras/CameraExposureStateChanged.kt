package nebulosa.indi.devices.cameras

import nebulosa.indi.protocol.PropertyState

data class CameraExposureStateChanged(
    override val device: Camera,
    val prevState: PropertyState,
) : CameraEvent
