package nebulosa.api.wizard.flat

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import nebulosa.api.cameras.CameraCaptureEvent
import nebulosa.api.message.MessageEvent
import nebulosa.indi.device.camera.Camera

data class FlatWizardEvent(
    @JvmField val camera: Camera,
    @JvmField var state: FlatWizardState = FlatWizardState.IDLE,
    @JvmField var exposureTime: Long = 0L,
    @JvmField var filter: Int = 0,
    @JvmField @field:JsonIgnoreProperties("camera") val capture: CameraCaptureEvent = CameraCaptureEvent(camera),
) : MessageEvent {

    override val eventName = "FLAT_WIZARD.ELAPSED"
}
