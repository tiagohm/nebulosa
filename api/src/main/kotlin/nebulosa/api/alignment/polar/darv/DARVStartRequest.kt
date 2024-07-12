package nebulosa.api.alignment.polar.darv

import nebulosa.api.cameras.CameraStartCaptureRequest
import nebulosa.guiding.GuideDirection

data class DARVStartRequest(
    @JvmField val capture: CameraStartCaptureRequest = CameraStartCaptureRequest.EMPTY,
    @JvmField val direction: GuideDirection = GuideDirection.NORTH,
    @JvmField val reversed: Boolean = false,
)
