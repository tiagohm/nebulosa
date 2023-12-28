package nebulosa.api.wizard.flat

import io.reactivex.rxjava3.subjects.PublishSubject
import nebulosa.api.cameras.CameraCaptureEvent
import nebulosa.api.cameras.CameraCaptureEventHandler
import nebulosa.api.messages.MessageEvent
import nebulosa.batch.processing.PublishSubscribe
import nebulosa.batch.processing.SimpleJob
import java.nio.file.Path
import java.time.Duration

data class FlatWizardJob(@JvmField val request: FlatWizardRequest) : SimpleJob(), PublishSubscribe<MessageEvent>, FlatWizardExecutionListener {

    @JvmField val camera = request.captureRequest.camera
    @JvmField val wheel = request.captureRequest.wheel

    private val cameraCaptureEventHandler = CameraCaptureEventHandler(this)
    private val step = FlatWizardStep(request)

    override val subject = PublishSubject.create<MessageEvent>()

    init {
        step.registerCameraCaptureListener(cameraCaptureEventHandler)
        step.registerFlatWizardExecutionListener(this)
        add(step)
    }

    override fun onFlatCaptured(step: FlatWizardStep, savedPath: Path, duration: Duration) {
        subject.onNext(FlatFrameCaptured(savedPath, duration))
    }

    override fun onFlatFailed(step: FlatWizardStep) {
        subject.onNext(FlatWizardFailed)
    }

    override fun onNext(event: MessageEvent) {
        if (event is CameraCaptureEvent) {
            super.onNext(FlatWizardElapsed(step.cameraExposureStep!!.exposureTime, event))
        }
    }
}
