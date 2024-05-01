package nebulosa.api.guiding

import java.time.Duration

data class DitherAfterExposureEvent(
    @JvmField val task: DitherAfterExposureTask,
    @JvmField val state: DitherAfterExposureState = DitherAfterExposureState.IDLE,
    @JvmField val dx: Double = 0.0, @JvmField val dy: Double = 0.0,
    @JvmField val elapsedTime: Duration = Duration.ZERO,
)
