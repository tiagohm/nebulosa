package nebulosa.api.alignment.polar.darv

import com.fasterxml.jackson.annotation.JsonIgnore
import nebulosa.guiding.GuideDirection
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.guide.GuideOutput
import org.hibernate.validator.constraints.Range

data class DARVStart(
    @JsonIgnore val camera: Camera? = null,
    @JsonIgnore val guideOutput: GuideOutput? = null,
    @Range(min = 1, max = 600) val exposureInSeconds: Long = 0L,
    @Range(min = 1, max = 60) val initialPauseInSeconds: Long = 0L,
    val direction: GuideDirection = GuideDirection.NORTH,
    val reversed: Boolean = false,
)
