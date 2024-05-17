package nebulosa.api.wizard.flat

import nebulosa.api.cameras.AutoSubFolderMode
import nebulosa.api.cameras.CameraCaptureEvent
import nebulosa.api.cameras.CameraCaptureState
import nebulosa.api.cameras.CameraCaptureTask
import nebulosa.api.messages.MessageEvent
import nebulosa.api.tasks.AbstractTask
import nebulosa.common.concurrency.cancel.CancellationToken
import nebulosa.fits.fits
import nebulosa.image.Image
import nebulosa.image.algorithms.computation.Statistics
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.camera.CameraEvent
import nebulosa.indi.device.camera.FrameType
import nebulosa.log.loggerFor
import java.nio.file.Path
import java.time.Duration

data class FlatWizardTask(
    @JvmField val camera: Camera,
    @JvmField val request: FlatWizardRequest,
) : AbstractTask<MessageEvent>() {

    private val meanTarget = request.meanTarget / 65535f
    private val meanRange = (meanTarget * request.meanTolerance / 100f).let { (meanTarget - it)..(meanTarget + it) }

    @Volatile private var cameraCaptureTask: CameraCaptureTask? = null
    @Volatile private var exposureMin = request.exposureMin
    @Volatile private var exposureMax = request.exposureMax
    @Volatile private var exposureTime = Duration.ZERO

    @Volatile private var state = FlatWizardState.IDLE
    @Volatile private var capture: CameraCaptureEvent? = null
    @Volatile private var savedPath: Path? = null

    fun handleCameraEvent(event: CameraEvent) {
        cameraCaptureTask?.handleCameraEvent(event)
    }

    override fun canUseAsLastEvent(event: MessageEvent) = event is FlatWizardEvent

    override fun execute(cancellationToken: CancellationToken) {
        while (!cancellationToken.isDone) {
            val delta = exposureMax.toMillis() - exposureMin.toMillis()

            if (delta < 10) {
                LOG.warn("Failed to find an optimal exposure time. exposureMin={}, exposureMax={}", exposureMin, exposureMax)
                state = FlatWizardState.FAILED
                break
            }

            exposureTime = (exposureMax + exposureMin).dividedBy(2L)

            LOG.info("Flat Wizard started. camera={}, request={}, exposureTime={}", camera, request, exposureTime)

            val cameraRequest = request.capture.copy(
                exposureTime = exposureTime, frameType = FrameType.FLAT,
                autoSave = false, autoSubFolderMode = AutoSubFolderMode.OFF,
            )

            state = FlatWizardState.EXPOSURING

            CameraCaptureTask(camera, cameraRequest).use {
                cameraCaptureTask = it

                it.subscribe { event ->
                    capture = event

                    if (event.state == CameraCaptureState.EXPOSURE_FINISHED) {
                        savedPath = event.savePath!!
                        onNext(event)
                    }

                    sendEvent()
                }

                it.execute(cancellationToken)
            }

            if (cancellationToken.isDone) {
                state = FlatWizardState.IDLE
                break
            } else if (savedPath == null) {
                state = FlatWizardState.FAILED
                break
            }

            val image = savedPath!!.fits().use { Image.open(it, false) }

            val statistics = STATISTICS.compute(image)
            LOG.info("flat frame captured. exposureTime={}, statistics={}", exposureTime, statistics)

            if (statistics.mean in meanRange) {
                state = FlatWizardState.CAPTURED
                LOG.info("found an optimal exposure time. exposureTime={}, path={}", exposureTime, savedPath)
                break
            } else if (statistics.mean < meanRange.start) {
                savedPath = null
                exposureMin = exposureTime
                LOG.info("captured frame is below mean range. exposureTime={}, path={}", exposureTime, savedPath)
            } else {
                savedPath = null
                exposureMax = exposureTime
                LOG.info("captured frame is above mean range. exposureTime={}, path={}", exposureTime, savedPath)
            }
        }

        if (state != FlatWizardState.FAILED && cancellationToken.isDone) {
            state = FlatWizardState.IDLE
        }

        sendEvent()

        LOG.info("Flat Wizard finished. camera={}, request={}, exposureTime={}", camera, request, exposureTime)
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun sendEvent() {
        onNext(FlatWizardEvent(state, exposureTime, capture, savedPath))
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<FlatWizardTask>()
        @JvmStatic private val STATISTICS = Statistics(noMedian = true, noDeviation = true)
    }
}
