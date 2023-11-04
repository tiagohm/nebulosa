package nebulosa.api.cameras

import nebulosa.api.sequencer.SequenceStepEvent

sealed interface CameraExposureEvent : CameraCaptureEvent, SequenceStepEvent {

    override val tasklet: CameraStartCaptureTasklet

    val exposureCount: Int

    val remainingTime: Long

    val exposureTime
        get() = tasklet.request.exposureInMicroseconds

    val exposureAmount
        get() = tasklet.request.exposureAmount
}
