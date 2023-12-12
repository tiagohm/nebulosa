package nebulosa.api.cameras

import nebulosa.batch.processing.JobExecutionListener
import nebulosa.batch.processing.Step

sealed interface CameraStartCaptureStep : Step, JobExecutionListener {

    val request: CameraStartCaptureRequest

    fun registerListener(listener: CameraCaptureListener)

    fun unregisterListener(listener: CameraCaptureListener)
}
