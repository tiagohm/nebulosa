package nebulosa.api.cameras

import nebulosa.api.sequencer.SequenceStepEvent
import java.time.Duration

sealed interface CameraExposureEvent : CameraCaptureEvent, SequenceStepEvent {

    override val tasklet: CameraStartCaptureTasklet

    val exposureCount: Int

    val remainingTime: Duration

    val exposureTime
        get() = tasklet.request.exposureTime

    val exposureAmount
        get() = tasklet.request.exposureAmount
}
