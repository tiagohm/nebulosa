package nebulosa.api.cameras

import nebulosa.batch.processing.StepExecution
import nebulosa.indi.device.camera.Camera

data class CameraExposureFinished(
    override val camera: Camera,
    override val stepExecution: StepExecution,
) : CameraExposureEvent {

    override val eventName = "CAMERA_EXPOSURE_FINISHED"
}
