package nebulosa.api.cameras

import nebulosa.api.guiding.DitherAfterExposureStep
import nebulosa.api.guiding.WaitForSettleStep
import nebulosa.batch.processing.SimpleJob
import nebulosa.batch.processing.SimpleSplitStep
import nebulosa.batch.processing.delay.DelayStep
import nebulosa.guiding.Guider

data class CameraCaptureJob(
    private val request: CameraStartCaptureRequest,
    private val guider: Guider,
) : SimpleJob() {

    private val cameraExposureStep = if (request.isLoop) CameraLoopExposureStep(request)
    else CameraExposureStep(request)

    override val id = "CameraCapture.Job.${System.currentTimeMillis()}"

    init {
        if (cameraExposureStep is CameraExposureStep) {
            val waitForSettleStep = WaitForSettleStep(guider)
            val ditherStep = DitherAfterExposureStep(request.dither)
            val cameraDelayStep = DelayStep(request.exposureDelay)
            val delayAndWaitForSettleStep = SimpleSplitStep(cameraDelayStep, waitForSettleStep)

            cameraDelayStep.registerDelayStepListener(cameraExposureStep)

            add(waitForSettleStep)
            add(cameraExposureStep)

            repeat(request.exposureAmount - 1) {
                add(delayAndWaitForSettleStep)
                add(cameraExposureStep)
                add(ditherStep)
            }
        } else {
            add(cameraExposureStep)
        }
    }

    fun registerListener(listener: CameraCaptureListener) {
        cameraExposureStep.registerCameraCaptureListener(listener)
    }

    fun unregisterListener(listener: CameraCaptureListener) {
        cameraExposureStep.unregisterCameraCaptureListener(listener)
    }
}
