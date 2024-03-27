package nebulosa.api.wizard.flat

import io.reactivex.rxjava3.subjects.PublishSubject
import nebulosa.api.cameras.CameraCaptureElapsed
import nebulosa.api.cameras.CameraCaptureEventHandler
import nebulosa.api.cameras.CameraExposureFinished
import nebulosa.api.image.ImageBucket
import nebulosa.api.messages.MessageEvent
import nebulosa.batch.processing.PublishSubscribe
import nebulosa.batch.processing.SimpleJob
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.filterwheel.FilterWheel
import java.nio.file.Path
import java.time.Duration

data class FlatWizardJob(
    @JvmField val camera: Camera,
    @JvmField val request: FlatWizardRequest,
    @JvmField val wheel: FilterWheel? = null,
    @JvmField val imageBucket: ImageBucket? = null,
) : SimpleJob(), PublishSubscribe<MessageEvent>, FlatWizardExecutionListener {

    private val cameraCaptureEventHandler = CameraCaptureEventHandler(this)
    private val step = FlatWizardStep(camera, request, imageBucket)

    override val subject = PublishSubject.create<MessageEvent>()

    init {
        step.registerCameraCaptureListener(cameraCaptureEventHandler)
        step.registerFlatWizardExecutionListener(this)
        register(step)
    }

    override fun onFlatCaptured(step: FlatWizardStep, savedPath: Path, duration: Duration) {
        super.onNext(FlatWizardFrameCaptured(duration, savedPath))
    }

    override fun onFlatFailed(step: FlatWizardStep) {
        super.onNext(FlatWizardFailed)
    }

    override fun onNext(event: MessageEvent) {
        if (event is CameraCaptureElapsed) {
            super.onNext(FlatWizardIsExposuring(step.exposureTime, event))

            // Notify Camera window to retrieve new image.
            if (event is CameraExposureFinished) {
                super.onNext(event)
            }
        }
    }

    override fun contains(data: Any): Boolean {
        return data === camera || data === wheel || super.contains(data)
    }
}
