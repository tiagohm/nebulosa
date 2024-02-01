package nebulosa.api.alignment.polar.darv

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import nebulosa.api.cameras.CameraStartCaptureRequest
import nebulosa.guiding.GuideDirection
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.guide.GuideOutput
import org.hibernate.validator.constraints.time.DurationMax
import org.hibernate.validator.constraints.time.DurationMin
import org.springframework.boot.convert.DurationUnit
import java.time.Duration
import java.time.temporal.ChronoUnit

data class DARVStartRequest(
    @JsonIgnore val camera: Camera? = null,
    @JsonIgnore val guideOutput: GuideOutput? = null,
    @JsonIgnoreProperties("camera", "focuser", "wheel") val capture: CameraStartCaptureRequest = CameraStartCaptureRequest.EMPTY,
    @field:DurationMin(seconds = 1) @field:DurationMax(seconds = 600) @field:DurationUnit(ChronoUnit.SECONDS) val exposureTime: Duration = Duration.ZERO,
    @field:DurationMin(seconds = 1) @field:DurationMax(seconds = 60) @field:DurationUnit(ChronoUnit.SECONDS) val initialPause: Duration = Duration.ZERO,
    val direction: GuideDirection = GuideDirection.NORTH,
    val reversed: Boolean = false,
)
