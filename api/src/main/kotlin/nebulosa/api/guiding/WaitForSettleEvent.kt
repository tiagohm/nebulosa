package nebulosa.api.guiding

import java.time.Duration

data class WaitForSettleEvent(
    @JvmField val task: WaitForSettleTask,
    @JvmField val elapsedTime: Duration = Duration.ZERO,
)
