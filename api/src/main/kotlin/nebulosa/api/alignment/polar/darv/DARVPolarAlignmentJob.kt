package nebulosa.api.alignment.polar.darv

import nebulosa.api.cameras.CameraExposureStep
import nebulosa.api.cameras.CameraStartCaptureRequest
import nebulosa.api.guiding.GuidePulseRequest
import nebulosa.api.guiding.GuidePulseStep
import nebulosa.batch.processing.SimpleJob
import nebulosa.batch.processing.delay.DelayStep

data class DARVPolarAlignmentJob(
    val request: DARVStart,
    val cameraRequest: CameraStartCaptureRequest,
) : SimpleJob() {

    init {
        val guideOutput = requireNotNull(request.guideOutput)

        val cameraExposureStep = CameraExposureStep(cameraRequest)
        cameraExposureStep.registerListener(this)

        val guidePulseDuration = request.exposureTime.dividedBy(2L)
        val initialPauseDelayStep = DelayStep(request.initialPause)
        initialPauseDelayStep.registerListener(this)

        val direction = if (request.reversed) request.direction.reversed else request.direction

        val forwardGuidePulseRequest = GuidePulseRequest(guideOutput, direction, guidePulseDuration)
        val forwardGuidePulseStep = GuidePulseStep(forwardGuidePulseRequest)
        forwardGuidePulseStep.registerListener(this)

        val backwardGuidePulseRequest = GuidePulseRequest(guideOutput, direction.reversed, guidePulseDuration)
        val backwardGuidePulseStep = GuidePulseStep(backwardGuidePulseRequest)
        backwardGuidePulseStep.registerListener(this)
    }
}
