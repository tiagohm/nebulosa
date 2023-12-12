package nebulosa.api.cameras

import nebulosa.batch.processing.JobExecution
import nebulosa.indi.device.camera.Camera

data class CameraCaptureFinished(
    override val camera: Camera,
    override val jobExecution: JobExecution,
) : CameraCaptureEvent {

    override val eventName = "CAMERA_CAPTURE_FINISHED"
}
