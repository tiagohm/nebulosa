package nebulosa.api.cameras

import nebulosa.job.manager.Job
import java.nio.file.Path

data class CameraExposureFinished(
    override val job: Job,
    override val task: CameraExposureTask,
    override val savedPath: Path,
) : CameraExposureEvent {

    override val elapsedTime = task.exposureTimeInMicroseconds
    override val remainingTime = 0L
    override val progress = 1.0
}
