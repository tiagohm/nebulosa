package nebulosa.api.alignment.polar.tppa

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import nebulosa.api.cameras.CameraStartCaptureRequest
import nebulosa.api.solver.PlateSolverOptions
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.mount.Mount

data class TPPAStartRequest(
    @JsonIgnore val camera: Camera? = null,
    @JsonIgnore val mount: Mount? = null,
    @JsonIgnoreProperties("camera", "focuser", "wheel") val capture: CameraStartCaptureRequest = CameraStartCaptureRequest.EMPTY,
    @field:NotNull @Valid val plateSolverOptions: PlateSolverOptions = PlateSolverOptions.EMPTY,
    val startFromCurrentPosition: Boolean = true,
    val eastDirection: Boolean = true,
    val refractionAdjustment: Boolean = false,
    val stopTrackingWhenDone: Boolean = true,
)
