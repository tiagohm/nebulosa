package nebulosa.api.guiding

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.validation.constraints.Positive
import nebulosa.guiding.GuideDirection
import nebulosa.indi.device.guide.GuideOutput

data class GuidePulseRequest(
    @JsonIgnore val guideOutput: GuideOutput? = null,
    val direction: GuideDirection,
    @field:Positive val durationInMilliseconds: Long,
)
