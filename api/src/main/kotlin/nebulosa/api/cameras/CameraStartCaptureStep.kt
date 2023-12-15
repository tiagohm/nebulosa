package nebulosa.api.cameras

import nebulosa.batch.processing.JobExecutionListener
import nebulosa.batch.processing.Step

sealed interface CameraStartCaptureStep : Step, JobExecutionListener {

    val request: CameraStartCaptureRequest

    fun registerCameraCaptureListener(listener: CameraCaptureListener): Boolean

    fun unregisterCameraCaptureListener(listener: CameraCaptureListener): Boolean
}
