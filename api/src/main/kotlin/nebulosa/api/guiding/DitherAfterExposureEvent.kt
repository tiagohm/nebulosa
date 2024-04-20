package nebulosa.api.guiding

import java.time.Duration

sealed interface DitherAfterExposureEvent {

    val task: DitherAfterExposureTask

    data class Started(override val task: DitherAfterExposureTask) : DitherAfterExposureEvent

    data class Dithered(
        override val task: DitherAfterExposureTask,
        @JvmField val dx: Double, @JvmField val dy: Double
    ) : DitherAfterExposureEvent

    data class Finished(
        override val task: DitherAfterExposureTask,
        @JvmField val elapsedTime: Duration
    ) : DitherAfterExposureEvent
}
