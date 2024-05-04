package nebulosa.api.guiding

import java.time.Duration

data class GuidePulseEvent(
    @JvmField val task: GuidePulseTask,
    @JvmField val remainingTime: Duration = Duration.ZERO,
    @JvmField val progress: Double = 0.0,
)
