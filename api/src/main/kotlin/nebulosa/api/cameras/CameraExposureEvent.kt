package nebulosa.api.cameras

import java.nio.file.Path
import java.time.Duration

sealed interface CameraExposureEvent {

    val task: CameraExposureTask

    data class Started(override val task: CameraExposureTask) : CameraExposureEvent

    data class Elapsed(
        override val task: CameraExposureTask,
        @JvmField val elapsedTime: Duration,
        @JvmField val remainingTime: Duration,
        @JvmField val progress: Double,
    ) : CameraExposureEvent

    data class Finished(
        override val task: CameraExposureTask,
        @JvmField val savePath: Path
    ) : CameraExposureEvent

    data class Aborted(override val task: CameraExposureTask) : CameraExposureEvent
}
