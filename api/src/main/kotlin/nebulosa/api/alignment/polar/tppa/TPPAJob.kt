package nebulosa.api.alignment.polar.tppa

import io.reactivex.rxjava3.subjects.PublishSubject
import nebulosa.api.cameras.AutoSubFolderMode
import nebulosa.api.cameras.CameraCaptureEventHandler
import nebulosa.api.cameras.CameraCaptureListener
import nebulosa.api.messages.MessageEvent
import nebulosa.batch.processing.PublishSubscribe
import nebulosa.batch.processing.SimpleJob
import nebulosa.imaging.Image
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.camera.FrameType
import nebulosa.indi.device.mount.Mount
import nebulosa.plate.solving.PlateSolver
import nebulosa.star.detection.StarDetector
import java.nio.file.Files
import java.time.Duration

data class TPPAJob(
    @JvmField val camera: Camera,
    @JvmField val request: TPPAStartRequest,
    @JvmField val solver: PlateSolver,
    @JvmField val starDetector: StarDetector<Image>,
    @JvmField val mount: Mount? = null,
) : SimpleJob(), PublishSubscribe<MessageEvent>, CameraCaptureListener {

    @JvmField val cameraRequest = request.capture.copy(
        savePath = Files.createTempDirectory("tppa"),
        exposureAmount = 1, exposureDelay = Duration.ZERO,
        frameType = FrameType.LIGHT, autoSave = false, autoSubFolderMode = AutoSubFolderMode.OFF
    )

    override val subject = PublishSubject.create<MessageEvent>()

    private val cameraCaptureEventHandler = CameraCaptureEventHandler(this)
    private val tppaStep = TPPAStep(camera, solver, starDetector, request, mount, cameraRequest)

    init {
        tppaStep.registerCameraCaptureListener(cameraCaptureEventHandler)

        register(tppaStep)
    }

    override fun contains(data: Any): Boolean {
        return data === camera || data === mount || super.contains(data)
    }
}
