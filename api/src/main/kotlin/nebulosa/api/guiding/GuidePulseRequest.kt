package nebulosa.api.guiding

import nebulosa.guiding.GuideDirection
import java.time.Duration

data class GuidePulseRequest(
    @JvmField val direction: GuideDirection,
    @JvmField val duration: Duration,
)
