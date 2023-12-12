package nebulosa.api.cameras

import nebulosa.api.guiding.DitherAfterExposureStep
import nebulosa.api.guiding.WaitForSettleStep
import nebulosa.batch.processing.SimpleFlowStep
import nebulosa.batch.processing.SimpleJob
import nebulosa.batch.processing.Step
import nebulosa.batch.processing.delay.DelayStep
import nebulosa.guiding.Guider

data class CameraCaptureJob(
    private val request: CameraStartCaptureRequest,
    private val guider: Guider,
) : SimpleJob() {

    private val cameraExposureStep = if (request.isLoop) CameraLoopExposureStep(request)
    else CameraExposureStep(request)

    override val id = "CameraCapture.Job.${System.currentTimeMillis()}"

    override val steps = ArrayList<Step>()

    init {
        if (cameraExposureStep is CameraExposureStep) {
            val waitForSettleStep = WaitForSettleStep(guider)
            val ditherStep = DitherAfterExposureStep(request.dither)
            val cameraDelayStep = DelayStep(request.exposureDelay)
            val delayAndWaitForSettleStep = DelayAndWaitForSettleStep(listOf(cameraDelayStep, waitForSettleStep))

            cameraDelayStep.registerListener(cameraExposureStep)

            steps.add(waitForSettleStep)
            steps.add(cameraExposureStep)

            repeat(request.exposureAmount - 1) {
                steps.add(delayAndWaitForSettleStep)
                steps.add(cameraExposureStep)
                steps.add(ditherStep)
            }
        } else {
            steps.add(cameraExposureStep)
        }
    }

    fun registerListener(listener: CameraCaptureListener) {
        cameraExposureStep.registerListener(listener)
    }

    fun unregisterListener(listener: CameraCaptureListener) {
        cameraExposureStep.unregisterListener(listener)
    }

    data class DelayAndWaitForSettleStep(override val steps: Collection<Step>) : SimpleFlowStep()
}
