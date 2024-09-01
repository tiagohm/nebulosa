package nebulosa.api.cameras

import nebulosa.job.manager.Job

data class CameraExposureElapsed(
    override val job: Job,
    override val task: CameraExposureTask,
    override val elapsedTime: Long,
    override val remainingTime: Long,
    override val progress: Double,
) : CameraExposureEvent {

    override val savedPath = null
}
