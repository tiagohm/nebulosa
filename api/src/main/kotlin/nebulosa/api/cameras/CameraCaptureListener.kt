package nebulosa.api.cameras

import nebulosa.batch.processing.JobExecution
import nebulosa.batch.processing.StepExecution

interface CameraCaptureListener {

    fun onCaptureStarted(step: CameraExposureStep, jobExecution: JobExecution) = Unit

    fun onExposureStarted(step: CameraExposureStep, stepExecution: StepExecution) = Unit

    fun onExposureElapsed(step: CameraExposureStep, stepExecution: StepExecution) = Unit

    fun onExposureFinished(step: CameraExposureStep, stepExecution: StepExecution) = Unit

    fun onCaptureFinished(step: CameraExposureStep, jobExecution: JobExecution) = Unit
}
