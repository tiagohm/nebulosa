package nebulosa.api.mounts

import java.time.Duration

data class MountMoveEvent(
    @JvmField val task: MountMoveTask,
    @JvmField val remainingTime: Duration = Duration.ZERO,
    @JvmField val progress: Double = 0.0,
)
