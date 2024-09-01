package nebulosa.api.cameras

import nebulosa.job.manager.TaskEvent
import java.nio.file.Path

sealed interface CameraExposureEvent : TaskEvent {

    val elapsedTime: Long

    val remainingTime: Long

    val progress: Double

    val savedPath: Path?
}
