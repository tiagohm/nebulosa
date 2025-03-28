package nebulosa.api.wizard.flat

import nebulosa.api.cameras.AutoSubFolderMode
import nebulosa.api.cameras.CameraCaptureState
import nebulosa.api.cameras.CameraEventAware
import nebulosa.api.cameras.CameraExposureEvent
import nebulosa.api.cameras.CameraExposureFinished
import nebulosa.api.cameras.CameraExposureTask
import nebulosa.api.message.MessageEvent
import nebulosa.api.wheels.WheelMoveTask
import nebulosa.fits.fits
import nebulosa.image.Image
import nebulosa.image.algorithms.computation.Statistics
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.camera.CameraEvent
import nebulosa.indi.device.camera.FrameType
import nebulosa.indi.device.filterwheel.FilterWheel
import nebulosa.job.manager.AbstractJob
import nebulosa.job.manager.Task
import nebulosa.log.d
import nebulosa.log.loggerFor
import nebulosa.util.concurrency.cancellation.CancellationSource
import nebulosa.util.concurrency.latch.CountUpDownLatch
import java.nio.file.Path
import java.time.Duration

data class FlatWizardJob(
    @JvmField val flatWizardExecutor: FlatWizardExecutor,
    @JvmField val camera: Camera,
    @JvmField val request: FlatWizardRequest,
    @JvmField val wheel: FilterWheel? = null,
) : AbstractJob(), CameraEventAware {

    @JvmField val meanTarget = request.meanTarget / 65535f
    @JvmField val meanRange = (meanTarget * request.meanTolerance / 100f).let { (meanTarget - it)..(meanTarget + it) }

    @Volatile private var exposureMin = request.exposureMin.toNanos()
    @Volatile private var exposureMax = request.exposureMax.toNanos()
    private val waitToComputeOptimalExposureTime = CountUpDownLatch()

    @Volatile private var cameraExposureTask = CameraExposureTask(
        this, camera, request.capture.copy(
            exposureTime = Duration.ofNanos((exposureMin + exposureMax) / 2),
            frameType = FrameType.FLAT,
            autoSave = true, autoSubFolderMode = AutoSubFolderMode.OFF,
        )
    )

    @JvmField val status = FlatWizardEvent(camera)

    inline val savedPath
        get() = status.capture.savedPath

    init {
        status.exposureTime = cameraExposureTask.request.exposureTime.toNanos() / 1000L

        if (request.filters.isNotEmpty() && wheel != null) {
            status.filter = 0
            add(WheelMoveTask(this, wheel, request.filters[0]))
        }

        add(cameraExposureTask)
    }

    override fun handleCameraEvent(event: CameraEvent) {
        cameraExposureTask.handleCameraEvent(event)
    }

    override fun onCancel(source: CancellationSource) {
        waitToComputeOptimalExposureTime.reset()
        super.onCancel(source)
    }

    private fun addCameraExposureTask() {
        val exposureTimeInNanos = (exposureMax + exposureMin) / 2L
        val request = cameraExposureTask.request.copy(exposureTime = Duration.ofNanos(exposureTimeInNanos))
        status.exposureTime = exposureTimeInNanos / 1000L
        cameraExposureTask = CameraExposureTask(this, camera, request)
        add(cameraExposureTask)
    }

    override fun accept(event: Any) {
        when (event) {
            is CameraExposureEvent -> {
                status.capture.handleCameraExposureEvent(event)

                if (event is CameraExposureFinished) {
                    status.capture.send()
                    computeOptimalExposureTime(event.savedPath)
                    waitToComputeOptimalExposureTime.reset()
                }

                status.send()
            }
        }
    }

    private fun computeOptimalExposureTime(savedPath: Path) {
        val image = savedPath.fits().use { Image.open(it, false) }
        val statistics = STATISTICS.compute(image)

        LOG.d { debug("flat frame computed. statistics={}", statistics) }

        if (statistics.mean in meanRange) {
            LOG.d { debug("found an optimal exposure time. exposureTime={}, path={}", status.exposureTime, savedPath) }
            status.state = FlatWizardState.CAPTURED
            status.capture.state = CameraCaptureState.IDLE

            // Go to next filter.
            if (request.filters.isNotEmpty() && status.filter < request.filters.size - 1 && wheel != null) {
                status.filter++
                add(WheelMoveTask(this, wheel, request.filters[status.filter]))

                exposureMin = request.exposureMin.toNanos()
                exposureMax = request.exposureMax.toNanos()
                addCameraExposureTask()
            }

            return
        } else if (statistics.mean < meanRange.start) {
            exposureMin = cameraExposureTask.request.exposureTime.toNanos()
            LOG.d { debug("captured frame is below mean range. exposureTime={}, path={}", exposureMin, savedPath) }
        } else {
            exposureMax = cameraExposureTask.request.exposureTime.toNanos()
            LOG.d { debug("captured frame is above mean range. exposureTime={}, path={}", exposureMax, savedPath) }
        }

        val delta = exposureMax - exposureMin

        // 10ms
        if (delta < MIN_DELTA_TIME) {
            LOG.warn("failed to find an optimal exposure time. min={}, max={}", exposureMin, exposureMax)
            status.state = FlatWizardState.FAILED
            status.capture.state = CameraCaptureState.IDLE
            return
        }

        addCameraExposureTask()
    }

    override fun beforeStart() {
        LOG.d { debug("Flat Wizard started. camera={}, request={}", camera, request) }

        val snoopedDevices = camera.snoopedDevices.filter { it !is FilterWheel }
        if (wheel != null) camera.snoop(snoopedDevices + wheel)
        else camera.snoop(snoopedDevices)

        status.state = FlatWizardState.EXPOSURING
        status.send()
    }

    override fun beforeTask(task: Task) {
        waitToComputeOptimalExposureTime.countUp()
    }

    override fun afterTask(task: Task, exception: Throwable?): Boolean {
        if (exception == null && task is CameraExposureTask && waitToComputeOptimalExposureTime.count > 0) {
            waitToComputeOptimalExposureTime.await()
        }

        return super.afterTask(task, exception)
    }

    override fun afterFinish() {
        if (status.state == FlatWizardState.EXPOSURING) {
            status.state = FlatWizardState.IDLE
            status.capture.state = CameraCaptureState.IDLE
            status.send()
        }

        LOG.d { debug("Flat Wizard finished. camera={}, request={}, exposureTime={} µs", camera, request, status.exposureTime) }
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun MessageEvent.send() {
        flatWizardExecutor.accept(this)
    }

    companion object {

        private const val MIN_DELTA_TIME = 10000000 // 10ms

        private val LOG = loggerFor<FlatWizardJob>()
        private val STATISTICS = Statistics(noMedian = true, noDeviation = true)
    }
}
