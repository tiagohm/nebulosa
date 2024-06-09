package nebulosa.api.alignment.polar.tppa

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import nebulosa.api.cameras.CameraStartCaptureRequest
import nebulosa.api.platesolver.PlateSolverRequest
import nebulosa.guiding.GuideDirection
import org.hibernate.validator.constraints.time.DurationMin
import org.springframework.boot.convert.DurationUnit
import java.time.Duration
import java.time.temporal.ChronoUnit

data class TPPAStartRequest(
    @JsonIgnoreProperties("camera", "focuser", "wheel") @JvmField val capture: CameraStartCaptureRequest = CameraStartCaptureRequest.EMPTY,
    @field:NotNull @Valid @JvmField val plateSolver: PlateSolverRequest = PlateSolverRequest.EMPTY,
    @JvmField val startFromCurrentPosition: Boolean = true,
    @JvmField val compensateRefraction: Boolean = false,
    @JvmField val stopTrackingWhenDone: Boolean = true,
    @field:DurationMin(seconds = 1L) @JvmField val stepDirection: GuideDirection = GuideDirection.EAST,
    @field:DurationUnit(ChronoUnit.SECONDS) @field:DurationMin(seconds = 1L) @JvmField val stepDuration: Duration = Duration.ZERO,
    @JvmField val stepSpeed: String? = null,
)
