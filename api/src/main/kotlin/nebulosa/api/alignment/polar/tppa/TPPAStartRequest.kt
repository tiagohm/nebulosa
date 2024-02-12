package nebulosa.api.alignment.polar.tppa

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import nebulosa.api.cameras.CameraStartCaptureRequest
import nebulosa.api.solver.PlateSolverOptions

data class TPPAStartRequest(
    @JsonIgnoreProperties("camera", "focuser", "wheel") val capture: CameraStartCaptureRequest = CameraStartCaptureRequest.EMPTY,
    @field:NotNull @Valid val plateSolver: PlateSolverOptions = PlateSolverOptions.EMPTY,
    val startFromCurrentPosition: Boolean = true,
    val eastDirection: Boolean = true,
    val compensateRefraction: Boolean = false,
    val stopTrackingWhenDone: Boolean = true,
    val stepDistance: Double = 10.0, // degrees
)
