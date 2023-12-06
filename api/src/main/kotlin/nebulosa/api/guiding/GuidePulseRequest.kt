package nebulosa.api.guiding

import com.fasterxml.jackson.annotation.JsonIgnore
import nebulosa.guiding.GuideDirection
import nebulosa.indi.device.guide.GuideOutput
import java.time.Duration

data class GuidePulseRequest(
    @JsonIgnore val guideOutput: GuideOutput? = null,
    val direction: GuideDirection,
    val duration: Duration,
)
