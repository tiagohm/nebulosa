package nebulosa.api.cameras

fun interface CameraCaptureEventListener {

    fun onCameraCaptureEvent(event: CameraCaptureEvent)
}
