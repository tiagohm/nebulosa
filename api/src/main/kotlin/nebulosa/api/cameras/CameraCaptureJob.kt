package nebulosa.api.cameras

import io.reactivex.rxjava3.subjects.PublishSubject
import nebulosa.api.guiding.DitherAfterExposureStep
import nebulosa.api.guiding.WaitForSettleStep
import nebulosa.api.messages.MessageEvent
import nebulosa.batch.processing.PublishSubscribe
import nebulosa.batch.processing.SimpleJob
import nebulosa.batch.processing.SimpleSplitStep
import nebulosa.batch.processing.delay.DelayStep
import nebulosa.guiding.Guider

data class CameraCaptureJob(
    @JvmField val request: CameraStartCaptureRequest,
    @JvmField val guider: Guider,
) : SimpleJob(), PublishSubscribe<MessageEvent> {

    private val cameraCaptureEventHandler = CameraCaptureEventHandler(this)

    @JvmField val camera = requireNotNull(request.camera)

    override val subject = PublishSubject.create<MessageEvent>()

    init {
        val cameraExposureStep = if (request.isLoop) CameraLoopExposureStep(request)
        else CameraExposureStep(request)

        if (cameraExposureStep is CameraExposureStep) {
            val ditherStep = DitherAfterExposureStep(request.dither, guider)
            val waitForSettleStep = WaitForSettleStep(guider)
            val cameraDelayStep = DelayStep(request.exposureDelay)
            val delayAndWaitForSettleStep = SimpleSplitStep(cameraDelayStep, waitForSettleStep)

            waitForSettleStep.registerWaitForSettleListener(cameraExposureStep)
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

        cameraExposureStep.registerCameraCaptureListener(cameraCaptureEventHandler)
    }
}
