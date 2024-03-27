package nebulosa.api.cameras

import nebulosa.batch.processing.JobExecution
import nebulosa.batch.processing.StepExecution
import nebulosa.image.format.ImageRepresentation
import java.nio.file.Path

interface CameraCaptureListener {

    fun onCaptureStarted(step: CameraExposureStep, jobExecution: JobExecution) = Unit

    fun onExposureStarted(step: CameraExposureStep, stepExecution: StepExecution) = Unit

    fun onExposureElapsed(step: CameraExposureStep, stepExecution: StepExecution) = Unit

    fun onExposureFinished(step: CameraExposureStep, stepExecution: StepExecution, image: ImageRepresentation, savedPath: Path) = Unit

    fun onCaptureFinished(step: CameraExposureStep, jobExecution: JobExecution) = Unit
}
