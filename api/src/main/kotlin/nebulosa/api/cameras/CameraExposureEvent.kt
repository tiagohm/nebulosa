package nebulosa.api.cameras

import nebulosa.batch.processing.StepExecution

sealed interface CameraExposureEvent : CameraCaptureEvent {

    val stepExecution: StepExecution

    override val jobExecution
        get() = stepExecution.jobExecution
}
