package nebulosa.api.wizard.flat

import nebulosa.api.cameras.AutoSubFolderMode
import nebulosa.api.cameras.CameraCaptureListener
import nebulosa.api.cameras.CameraExposureStep
import nebulosa.api.cameras.CameraExposureStep.Companion.makeSavePath
import nebulosa.batch.processing.Step
import nebulosa.batch.processing.StepExecution
import nebulosa.batch.processing.StepResult
import nebulosa.fits.fits
import nebulosa.image.Image
import nebulosa.image.algorithms.computation.Statistics
import nebulosa.indi.device.camera.Camera
import nebulosa.io.transferAndClose
import org.slf4j.LoggerFactory
import java.time.Duration
import java.util.concurrent.atomic.AtomicReference
import kotlin.io.path.deleteIfExists
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream

data class FlatWizardStep(
    @JvmField val camera: Camera,
    @JvmField val request: FlatWizardRequest,
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
            camera,
            request.captureRequest.copy(
                exposureTime = exposureTime, exposureAmount = 1,
                autoSave = false, autoSubFolderMode = AutoSubFolderMode.OFF,
            )
        )

        this.cameraExposureStep.set(cameraExposureStep)
        cameraCaptureListeners.forEach(cameraExposureStep::registerCameraCaptureListener)
        cameraExposureStep.executeSingle(stepExecution)

        val savedPath = cameraExposureStep.savedPath

        if (!stopped && savedPath != null) {
            image = savedPath.fits().use { image?.load(it, false) ?: Image.open(it, false) }

            val statistics = STATISTICS.compute(image!!)
            LOG.info("flat frame captured. duration={}, statistics={}", exposureTime, statistics)

            if (statistics.mean in meanRange) {
                val path = request.captureRequest.makeSavePath(camera, true)
                savedPath.inputStream().transferAndClose(path.outputStream())
                savedPath.deleteIfExists()
                LOG.info("Found an optimal exposure time. exposure={}, path={}", exposureTime, path)
                flatWizardExecutionListeners.forEach { it.onFlatCaptured(this, path, exposureTime) }
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
