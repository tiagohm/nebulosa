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
import nebulosa.indi.device.camera.Camera

data class CameraCaptureJob(
    @JvmField val camera: Camera,
    @JvmField val request: CameraStartCaptureRequest,
    @JvmField val guider: Guider,
) : SimpleJob(), PublishSubscribe<MessageEvent> {

    private val cameraCaptureEventHandler = CameraCaptureEventHandler(this)

    override val subject = PublishSubject.create<MessageEvent>()

    init {
        val cameraExposureStep = if (request.isLoop) CameraLoopExposureStep(camera, request)
        else CameraExposureStep(camera, request)

        if (cameraExposureStep is CameraExposureStep) {
            val ditherStep = DitherAfterExposureStep(request.dither, guider)
            val waitForSettleStep = WaitForSettleStep(guider)
            val cameraDelayStep = DelayStep(request.exposureDelay)
            val delayAndWaitForSettleStep = SimpleSplitStep(cameraDelayStep, waitForSettleStep)

            waitForSettleStep.registerWaitForSettleListener(cameraExposureStep)
            cameraDelayStep.registerDelayStepListener(cameraExposureStep)

            register(waitForSettleStep)
            register(cameraExposureStep)

            repeat(request.exposureAmount - 1) {
                register(delayAndWaitForSettleStep)
                register(cameraExposureStep)
                register(ditherStep)
            }
        } else {
            register(cameraExposureStep)
        }

        cameraExposureStep.registerCameraCaptureListener(cameraCaptureEventHandler)
    }

    override fun contains(data: Any): Boolean {
        return data === camera || super.contains(data)
    }
}
