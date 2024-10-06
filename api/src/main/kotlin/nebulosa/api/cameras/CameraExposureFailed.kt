package nebulosa.api.cameras

import nebulosa.job.manager.Job

data class CameraExposureFailed(
    override val job: Job,
    override val task: CameraExposureTask,
) : CameraExposureEvent {

    override val elapsedTime = task.exposureTimeInMicroseconds
    override val remainingTime = 0L
    override val progress = 1.0
    override val savedPath = null
}
