package nebulosa.api.alignment.polar.darv

import nebulosa.api.cameras.CameraCaptureEvent
import nebulosa.api.messages.MessageEvent
import nebulosa.guiding.GuideDirection
import nebulosa.indi.device.camera.Camera

data class DARVEvent(
    @JvmField val camera: Camera,
    @JvmField val state: DARVState = DARVState.IDLE,
    @JvmField val direction: GuideDirection? = null,
    @JvmField val capture: CameraCaptureEvent? = null,
) : MessageEvent {

    override val eventName = "DARV.ELAPSED"
}
