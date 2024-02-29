package nebulosa.api.alignment.polar.tppa

import io.reactivex.rxjava3.subjects.PublishSubject
import nebulosa.api.cameras.AutoSubFolderMode
import nebulosa.api.cameras.CameraCaptureEventHandler
import nebulosa.api.cameras.CameraCaptureListener
import nebulosa.api.messages.MessageEvent
import nebulosa.batch.processing.PublishSubscribe
import nebulosa.batch.processing.SimpleJob
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.camera.FrameType
import nebulosa.indi.device.mount.Mount
import nebulosa.math.Angle
import nebulosa.plate.solving.PlateSolver
import java.nio.file.Files
import java.time.Duration

data class TPPAJob(
    @JvmField val camera: Camera,
    @JvmField val request: TPPAStartRequest,
    @JvmField val solver: PlateSolver,
    @JvmField val mount: Mount? = null,
    @JvmField val longitude: Angle = mount!!.longitude,
    @JvmField val latitude: Angle = mount!!.latitude,
) : SimpleJob(), PublishSubscribe<MessageEvent>, CameraCaptureListener, TPPAListener {

    @JvmField val cameraRequest = request.capture.copy(
        savePath = Files.createTempDirectory("tppa"),
        exposureAmount = 1, exposureDelay = Duration.ZERO,
        exposureTime = maxOf(request.capture.exposureTime, MIN_EXPOSURE_TIME),
        frameType = FrameType.LIGHT, autoSave = false, autoSubFolderMode = AutoSubFolderMode.OFF
    )

    override val subject = PublishSubject.create<MessageEvent>()

    private val cameraCaptureEventHandler = CameraCaptureEventHandler(this)
    private val tppaStep = TPPAStep(camera, solver, request, mount, longitude, latitude, cameraRequest)

    init {
        tppaStep.registerCameraCaptureListener(cameraCaptureEventHandler)
        tppaStep.registerTPPAListener(this)

        register(tppaStep)
    }

    override fun slewStarted(step: TPPAStep, rightAscension: Angle, declination: Angle) {
        onNext(TPPAEvent.Slewing(id, step.stepCount, step.elapsedTime, rightAscension, declination))
    }

    override fun solverStarted(step: TPPAStep) {
        onNext(TPPAEvent.Solving(id, step.stepCount, step.elapsedTime))
    }

    override fun solverFinished(step: TPPAStep, rightAscension: Angle, declination: Angle) {
        onNext(TPPAEvent.Solved(id, step.stepCount, step.elapsedTime, rightAscension, declination))
    }

    override fun polarAlignmentPaused(step: TPPAStep) {
        onNext(TPPAEvent.Paused(id, step.stepCount, step.elapsedTime))
    }

    override fun polarAlignmentComputed(step: TPPAStep, azimuth: Angle, altitude: Angle) {
        val azimuthErrorDirection = when {
            azimuth > 0 -> if (latitude > 0) "🠔 Move LEFT/WEST" else "🠔 Move LEFT/EAST"
            azimuth < 0 -> if (latitude > 0) "Move RIGHT/EAST 🠖" else "Move RIGHT/WEST 🠖"
            else -> ""
        }

        val altitudeErrorDirection = when {
            altitude > 0 -> if (latitude > 0) "🠗 Move DOWN" else "Move UP 🠕"
            altitude < 0 -> if (latitude > 0) "Move UP 🠕" else "🠗 Move DOWN"
            else -> ""
        }

        onNext(TPPAEvent.Computed(id, step.stepCount, step.elapsedTime, azimuth, altitude, azimuthErrorDirection, altitudeErrorDirection))
    }

    override fun solverFailed(step: TPPAStep) {
        onNext(TPPAEvent.Failed(id, step.stepCount, step.elapsedTime))
    }

    override fun polarAlignmentFinished(step: TPPAStep, aborted: Boolean) {
        onNext(TPPAEvent.Finished(id))
    }

    override fun contains(data: Any): Boolean {
        return data === camera || data === mount || super.contains(data)
    }

    companion object {

        @JvmStatic private val MIN_EXPOSURE_TIME: Duration = Duration.ofSeconds(1L)
    }
}