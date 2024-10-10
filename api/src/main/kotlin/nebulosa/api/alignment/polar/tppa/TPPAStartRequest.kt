package nebulosa.api.alignment.polar.tppa

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import nebulosa.api.beans.converters.time.DurationUnit
import nebulosa.api.cameras.CameraStartCaptureRequest
import nebulosa.api.javalin.Validatable
import nebulosa.api.javalin.min
import nebulosa.api.platesolver.PlateSolverRequest
import nebulosa.guiding.GuideDirection
import java.time.Duration
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

data class TPPAStartRequest(
    @JsonIgnoreProperties("camera", "focuser", "wheel") @JvmField val capture: CameraStartCaptureRequest = CameraStartCaptureRequest.EMPTY,
    @JvmField val plateSolver: PlateSolverRequest = PlateSolverRequest.EMPTY,
    @JvmField val startFromCurrentPosition: Boolean = true,
    @JvmField val compensateRefraction: Boolean = false,
    @JvmField val stopTrackingWhenDone: Boolean = true,
    @JvmField val stepDirection: GuideDirection = GuideDirection.EAST,
    @field:DurationUnit(ChronoUnit.SECONDS) @JvmField val stepDuration: Duration = Duration.ZERO,
    @JvmField val stepSpeed: String? = null,
) : Validatable {

    override fun validate() {
        plateSolver.validate()
        stepDuration.min(1L, TimeUnit.SECONDS)
    }
}
