package nebulosa.api.alignment.polar.darv

import nebulosa.api.cameras.CameraStartCaptureRequest
import nebulosa.guiding.GuideDirection

data class DARVStartRequest(
    val capture: CameraStartCaptureRequest = CameraStartCaptureRequest.EMPTY,
    val direction: GuideDirection = GuideDirection.NORTH,
    val reversed: Boolean = false,
)
