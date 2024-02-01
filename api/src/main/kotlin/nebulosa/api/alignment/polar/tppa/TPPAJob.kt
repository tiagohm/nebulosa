package nebulosa.api.alignment.polar.tppa

import io.reactivex.rxjava3.subjects.PublishSubject
import nebulosa.api.cameras.AutoSubFolderMode
import nebulosa.api.cameras.CameraCaptureEventHandler
import nebulosa.api.cameras.CameraCaptureListener
import nebulosa.api.messages.MessageEvent
import nebulosa.batch.processing.PublishSubscribe
import nebulosa.batch.processing.SimpleJob
import nebulosa.imaging.Image
import nebulosa.indi.device.camera.FrameType
import nebulosa.plate.solving.PlateSolver
import nebulosa.star.detection.StarDetector
import java.nio.file.Files
import java.time.Duration

data class TPPAJob(
    @JvmField val request: TPPAStartRequest,
    @JvmField val solver: PlateSolver,
    @JvmField val starDetector: StarDetector<Image>,
) : SimpleJob(), PublishSubscribe<MessageEvent>, CameraCaptureListener {

    @JvmField val camera = requireNotNull(request.camera)
    @JvmField val mount = requireNotNull(request.mount)

    @JvmField val cameraRequest = request.capture.copy(
        camera = camera,
        savePath = Files.createTempDirectory("tppa"),
        exposureAmount = 1, exposureDelay = Duration.ZERO,
        frameType = FrameType.LIGHT, autoSave = false, autoSubFolderMode = AutoSubFolderMode.OFF
    )

    override val subject = PublishSubject.create<MessageEvent>()

    private val cameraCaptureEventHandler = CameraCaptureEventHandler(this)
    private val tppaStep = TPPAStep(solver, starDetector, mount, request, cameraRequest)

    init {
        tppaStep.registerCameraCaptureListener(cameraCaptureEventHandler)

        add(tppaStep)
    }
}
