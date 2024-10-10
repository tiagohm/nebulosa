package nebulosa.api.cameras

import nebulosa.job.manager.Job

data class CameraExposureStarted(override val job: Job, override val task: CameraExposureTask) : CameraExposureEvent {

    override val elapsedTime = 0L
    override val remainingTime = task.exposureTimeInMicroseconds
    override val progress = 0.0
    override val savedPath = null
}
