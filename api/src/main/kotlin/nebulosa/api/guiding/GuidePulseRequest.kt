package nebulosa.api.guiding

import nebulosa.guiding.GuideDirection
import java.time.Duration

data class GuidePulseRequest(
    val direction: GuideDirection,
    val duration: Duration,
)
