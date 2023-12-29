package nebulosa.api.wizard.flat

import nebulosa.api.cameras.AutoSubFolderMode
import nebulosa.api.cameras.CameraCaptureListener
import nebulosa.api.cameras.CameraExposureStep
import nebulosa.batch.processing.Step
import nebulosa.batch.processing.StepExecution
import nebulosa.batch.processing.StepResult
import nebulosa.fits.Fits
import nebulosa.imaging.Image
import nebulosa.imaging.algorithms.computation.Statistics
import org.slf4j.LoggerFactory

data class FlatWizardStep(
    @JvmField val request: FlatWizardRequest,
) : Step {

    @Volatile var exposureMin = request.exposureMin
        private set

    @Volatile var exposureMax = request.exposureMax
        private set

    @Volatile var cameraExposureStep: CameraExposureStep? = null
        private set

    @Volatile private var stopped = false
    @Volatile private var image: Image? = null

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
        val delta = exposureMax.toMillis() - exposureMin.toMillis()

        if (delta < 10) {
            flatWizardExecutionListeners.forEach { it.onFlatFailed(this) }
            return StepResult.FINISHED
        }

        val cameraExposureStep = CameraExposureStep(
            request.captureRequest.copy(
                exposureTime = (exposureMax + exposureMin).dividedBy(2L), exposureAmount = 1,
                autoSave = false, autoSubFolderMode = AutoSubFolderMode.OFF,
            )
        )

        this.cameraExposureStep = cameraExposureStep

        cameraCaptureListeners.forEach(cameraExposureStep::registerCameraCaptureListener)
        cameraExposureStep.execute(stepExecution)

        val savedPath = cameraExposureStep.savedPath

        if (!stopped && savedPath != null) {
            Fits(savedPath).use { fits ->
                image = image?.load(fits) ?: Image.open(fits, false)

                val statistics = STATISTICS.compute(image!!)

                if (statistics.mean in meanRange) {
                    flatWizardExecutionListeners.forEach { it.onFlatCaptured(this, savedPath, cameraExposureStep.exposureTime) }
                } else if (statistics.mean < meanRange.start) {
                    exposureMin = cameraExposureStep.exposureTime
                    return StepResult.CONTINUABLE
                } else {
                    exposureMax = cameraExposureStep.exposureTime
                    return StepResult.CONTINUABLE
                }
            }
        }

        return StepResult.FINISHED
    }

    override fun stop(mayInterruptIfRunning: Boolean) {
        stopped = true
        cameraExposureStep?.stop(mayInterruptIfRunning)
        cameraExposureStep = null
    }

    companion object {

        @JvmStatic private val STATISTICS = Statistics(noMedian = false, noDeviation = false)
        @JvmStatic private val LOG = LoggerFactory.getLogger(FlatWizardStep::class.java)
    }
}
