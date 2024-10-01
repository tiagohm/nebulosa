package nebulosa.api.wizard.flat

import nebulosa.api.cameras.AutoSubFolderMode
import nebulosa.api.cameras.CameraEventAware
import nebulosa.api.cameras.CameraExposureEvent
import nebulosa.api.cameras.CameraExposureFinished
import nebulosa.api.cameras.CameraExposureTask
import nebulosa.api.message.MessageEvent
import nebulosa.fits.fits
import nebulosa.image.Image
import nebulosa.image.algorithms.computation.Statistics
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.camera.CameraEvent
import nebulosa.indi.device.camera.FrameType
import nebulosa.job.manager.AbstractJob
import nebulosa.job.manager.Task
import nebulosa.log.debug
import nebulosa.log.loggerFor
import java.nio.file.Path
import java.time.Duration
import kotlin.use

data class FlatWizardJob(
    @JvmField val flatWizardExecutor: FlatWizardExecutor,
    @JvmField val camera: Camera,
    @JvmField val request: FlatWizardRequest,
) : AbstractJob(), CameraEventAware {

    @JvmField val meanTarget = request.meanTarget / 65535f
    @JvmField val meanRange = (meanTarget * request.meanTolerance / 100f).let { (meanTarget - it)..(meanTarget + it) }

    @Volatile private var exposureMin = request.exposureMin.toNanos()
    @Volatile private var exposureMax = request.exposureMax.toNanos()

    @JvmField val status = FlatWizardEvent(camera)

    @Volatile private var cameraRequest = request.capture.copy(
        exposureTime = Duration.ZERO, frameType = FrameType.FLAT,
        autoSave = false, autoSubFolderMode = AutoSubFolderMode.OFF,
    )

    @Volatile private var cameraExposureTask = CameraExposureTask(this, camera, cameraRequest)

    inline val savedPath
        get() = status.capture.savedPath

    init {
        status.capture.exposureAmount = 0

        add(cameraExposureTask)
    }

    override fun handleCameraEvent(event: CameraEvent) {
        cameraExposureTask.handleCameraEvent(event)
    }

    override fun accept(event: Any) {
        when (event) {
            is CameraExposureEvent -> {
                status.capture.handleCameraExposureEvent(event)

                if (event is CameraExposureFinished) {
                    status.capture.send()

                    status.state = FlatWizardState.CAPTURED
                    computeOptimalExposureTime(event.savedPath)
                }

                status.send()
            }
        }
    }

    private fun computeOptimalExposureTime(savedPath: Path) {
        val image = savedPath.fits().use { Image.open(it, false) }
        val statistics = STATISTICS.compute(image)

        LOG.debug { "flat frame computed. statistics=$statistics" }

        if (statistics.mean in meanRange) {
            LOG.debug { "found an optimal exposure time. exposureTime=${status.exposureTime}, path=$savedPath" }
            status.state = FlatWizardState.IDLE
            return stop()
        } else if (statistics.mean < meanRange.start) {
            exposureMin = status.exposureTime
            LOG.debug { "captured frame is below mean range. exposureTime=${status.exposureTime}, path=$savedPath" }
        } else {
            exposureMax = status.exposureTime
            LOG.debug { "captured frame is above mean range. exposureTime=${status.exposureTime}, path=$savedPath" }
        }

        val delta = exposureMax - exposureMin

        // 10ms
        if (delta < 10000000) {
            LOG.warn("Failed to find an optimal exposure time. exposureMin={}, exposureMax={}", exposureMin, exposureMax)
            status.state = FlatWizardState.FAILED
            return stop()
        }
    }

    override fun beforeTask(task: Task) {
        if (task === cameraExposureTask) {
            val exposureTimeInNanos = (exposureMax + exposureMin) / 2L
            cameraRequest = cameraRequest.copy(exposureTime = Duration.ofNanos(exposureTimeInNanos))
            status.exposureTime = exposureTimeInNanos / 1000L
        }
    }

    override fun beforeStart() {
        LOG.debug { "Flat Wizard started. camera=$camera, request=$request" }
    }

    override fun afterFinish() {
        LOG.debug { "Flat Wizard finished. camera=$camera, request=$request, exposureTime=${status.exposureTime} µs" }
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun MessageEvent.send() {
        flatWizardExecutor.accept(this)
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<FlatWizardJob>()
        @JvmStatic private val STATISTICS = Statistics(noMedian = true, noDeviation = true)
    }
}
