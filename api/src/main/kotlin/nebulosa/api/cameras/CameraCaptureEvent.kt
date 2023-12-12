package nebulosa.api.cameras

import nebulosa.api.services.MessageEvent
import nebulosa.batch.processing.JobExecution
import nebulosa.indi.device.camera.Camera

sealed interface CameraCaptureEvent : MessageEvent {

    val camera: Camera

    val jobExecution: JobExecution
}
