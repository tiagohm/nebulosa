package nebulosa.api.cameras

import java.nio.file.Path
import java.time.Duration

data class CameraExposureEvent(
    @JvmField val task: CameraExposureTask,
    @JvmField val state: CameraExposureState = CameraExposureState.IDLE,
    @JvmField val elapsedTime: Duration = Duration.ZERO,
    @JvmField val remainingTime: Duration = Duration.ZERO,
    @JvmField val progress: Double = 0.0,
    @JvmField val savedPath: Path? = null,
)
