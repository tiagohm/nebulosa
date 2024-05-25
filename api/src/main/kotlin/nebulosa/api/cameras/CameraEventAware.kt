package nebulosa.api.cameras

import nebulosa.indi.device.camera.CameraEvent

fun interface CameraEventAware {

    fun handleCameraEvent(event: CameraEvent)
}
