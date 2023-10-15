package nebulosa.api.cameras

import com.fasterxml.jackson.annotation.JsonIgnore
import nebulosa.api.sequencer.SequenceJobEvent
import nebulosa.indi.device.camera.Camera
import org.springframework.batch.core.JobExecution

data class CameraCaptureFinished(
    override val camera: Camera,
    @JsonIgnore override val jobExecution: JobExecution,
    @JsonIgnore override val tasklet: CameraExposureTasklet,
) : CameraCaptureEvent, SequenceJobEvent {

    @JsonIgnore override val eventName = "CAMERA_CAPTURE_FINISHED"
}
