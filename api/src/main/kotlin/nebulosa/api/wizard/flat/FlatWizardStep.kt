package nebulosa.api.wizard.flat

import nebulosa.api.cameras.AutoSubFolderMode
import nebulosa.api.cameras.CameraCaptureListener
import nebulosa.api.cameras.CameraExposureStep
import nebulosa.api.image.ImageBucket
import nebulosa.batch.processing.Step
import nebulosa.batch.processing.StepExecution
import nebulosa.batch.processing.StepResult
import nebulosa.fits.fits
import nebulosa.image.Image
import nebulosa.image.algorithms.computation.Statistics
import nebulosa.image.format.ImageRepresentation
import nebulosa.indi.device.camera.Camera
import org.slf4j.LoggerFactory
import java.nio.file.Path
import java.time.Duration
import java.util.concurrent.atomic.AtomicReference

data class FlatWizardStep(
    @JvmField val camera: Camera,
    @JvmField val request: FlatWizardRequest,
    @JvmField val imageBucket: ImageBucket? = null,
) : Step {

    @Volatile var exposureMin = request.exposureMin
        private set

    @Volatile var exposureMax = request.exposureMax
        private set

    @Volatile var exposureTime: Duration = Duration.ZERO
        private set

    @Volatile private var stopped = false
    @Volatile private var image: Image? = null

    private val cameraExposureStep = AtomicReference<CameraExposureStep>()
    private val flatWizardExecutionListeners = HashSet<FlatWizardExecutionListener>()
    private val cameraCaptureListeners = HashSet<CameraCaptureListener>()
    private val meanTarget = request.meanTarget / 65535f
    private val meanRange = (meanTarget * request.meanTolerance / 100f).let { (meanTarget - it)..(meanTarget + it) }

    fun registerFlatWizardExecutionListener(listener: FlatWizardExecutionListener) {
        flatWizardExecutionListeners.add(listener)
    }

    fun unregisterFlatWizardExecutionListener(listener: FlatWizardExecutionListener) {
        flatWizardExecutionListeners.remove(listener)
    }

    fun registerCameraCaptureListener(listener: CameraCaptureListener): Boolean {
        return cameraCaptureListeners.add(listener)
    }

    fun unregisterCameraCaptureListener(listener: CameraCaptureListener): Boolean {
        return cameraCaptureListeners.remove(listener)
    }

    override fun execute(stepExecution: StepExecution): StepResult {
        if (stopped) return StepResult.FINISHED

        val delta = exposureMax.toMillis() - exposureMin.toMillis()

        if (delta < 10) {
            LOG.warn("Failed to find an optimal exposure time. exposureMin={}, exposureMax={}", exposureMin, exposureMax)
            flatWizardExecutionListeners.forEach { it.onFlatFailed(this) }
            return StepResult.FINISHED
        }

        exposureTime = (exposureMax + exposureMin).dividedBy(2L)

        val cameraExposureStep = CameraExposureStep(
            camera, request.captureRequest.copy(
                exposureTime = exposureTime, exposureAmount = 1,
                autoSave = false, autoSubFolderMode = AutoSubFolderMode.OFF,
            )
        )

        var saved: Pair<ImageRepresentation?, Path>? = null

        val listener = object : CameraCaptureListener {

            override fun onExposureFinished(step: CameraExposureStep, stepExecution: StepExecution, image: ImageRepresentation?, savedPath: Path) {
                saved = image to savedPath
            }
        }

        this.cameraExposureStep.set(cameraExposureStep)
        cameraCaptureListeners.forEach(cameraExposureStep::registerCameraCaptureListener)
        cameraExposureStep.registerCameraCaptureListener(listener)
        cameraExposureStep.executeSingle(stepExecution)

        if (!stopped && saved != null) {
            val (imageRepresentation, savedPath) = saved!!

            image = if (imageRepresentation != null) image?.load(imageRepresentation) ?: Image.open(imageRepresentation, false)
            else savedPath.fits().use { image?.load(it) ?: Image.open(it, false) }

            imageBucket?.put(savedPath, image!!)

            val statistics = STATISTICS.compute(image!!)
            LOG.info("flat frame captured. duration={}, statistics={}", exposureTime, statistics)

            if (statistics.mean in meanRange) {
                LOG.info("Found an optimal exposure time. exposure={}, path={}", exposureTime, savedPath)
                flatWizardExecutionListeners.forEach { it.onFlatCaptured(this, savedPath, exposureTime) }
            } else if (statistics.mean < meanRange.start) {
                exposureMin = cameraExposureStep.exposureTime
                return StepResult.CONTINUABLE
            } else {
                exposureMax = cameraExposureStep.exposureTime
                return StepResult.CONTINUABLE
            }
        }

        return StepResult.FINISHED
    }

    override fun stop(mayInterruptIfRunning: Boolean) {
        stopped = true
        cameraExposureStep.getAndSet(null)?.stop(mayInterruptIfRunning)
    }

    companion object {

        @JvmStatic private val STATISTICS = Statistics(noMedian = true, noDeviation = true)
        @JvmStatic private val LOG = LoggerFactory.getLogger(FlatWizardStep::class.java)
    }
}
