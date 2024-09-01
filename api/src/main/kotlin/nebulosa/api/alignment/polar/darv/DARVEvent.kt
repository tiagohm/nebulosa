package nebulosa.api.alignment.polar.darv

import nebulosa.api.cameras.CameraCaptureEvent
import nebulosa.api.message.MessageEvent
import nebulosa.guiding.GuideDirection
import nebulosa.indi.device.camera.Camera

data class DARVEvent(
    @JvmField val camera: Camera,
    @JvmField var state: DARVState = DARVState.IDLE,
    @JvmField var direction: GuideDirection? = null,
    @JvmField val capture: CameraCaptureEvent = CameraCaptureEvent(camera),
) : MessageEvent {

    override val eventName = "DARV.ELAPSED"
}
