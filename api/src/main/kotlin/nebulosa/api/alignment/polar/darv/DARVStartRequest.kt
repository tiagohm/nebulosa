package nebulosa.api.alignment.polar.darv

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import nebulosa.api.cameras.CameraStartCaptureRequest
import nebulosa.guiding.GuideDirection
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.guide.GuideOutput
import org.hibernate.validator.constraints.time.DurationMax
import org.hibernate.validator.constraints.time.DurationMin
import java.time.Duration

data class DARVStartRequest(
    @JsonIgnoreProperties("camera") val capture: CameraStartCaptureRequest? = null,
    @JsonIgnore val camera: Camera? = null,
    @JsonIgnore val guideOutput: GuideOutput? = null,
    @field:DurationMin(seconds = 1) @field:DurationMax(seconds = 600) val exposureTime: Duration = Duration.ZERO,
    @field:DurationMin(seconds = 1) @field:DurationMax(seconds = 60) val initialPause: Duration = Duration.ZERO,
    val direction: GuideDirection = GuideDirection.NORTH,
    val reversed: Boolean = false,
)
