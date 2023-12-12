package nebulosa.api.cameras

import nebulosa.batch.processing.JobExecution
import nebulosa.batch.processing.StepExecution

interface CameraCaptureListener {

    fun onCaptureStarted(step: CameraExposureStep, jobExecution: JobExecution)

    fun onExposureStarted(stepExecution: StepExecution)

    fun onExposureElapsed(stepExecution: StepExecution)

    fun onExposureFinished(stepExecution: StepExecution)

    fun onCaptureFinished(step: CameraExposureStep, jobExecution: JobExecution)
}
