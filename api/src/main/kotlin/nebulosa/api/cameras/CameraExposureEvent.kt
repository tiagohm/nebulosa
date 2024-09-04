package nebulosa.api.cameras

import nebulosa.job.manager.TimedTaskEvent
import java.nio.file.Path

sealed interface CameraExposureEvent : TimedTaskEvent {

    val savedPath: Path?
}
