package nebulosa.api.guiding

import java.time.Duration

sealed interface GuidePulseEvent {

    data class Elapsed(
        @JvmField val remainingTime: Duration,
        @JvmField val progress: Double,
    ) : GuidePulseEvent
}
