package nebulosa.api.sequencer

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import nebulosa.api.cameras.CameraCaptureEvent
import nebulosa.api.message.MessageEvent
import nebulosa.indi.device.camera.Camera

data class SequencerEvent(
    @JvmField val camera: Camera,
    @JvmField var id: Int = 0,
    @JvmField var elapsedTime: Long = 0L,
    @JvmField var remainingTime: Long = 0L,
    @JvmField var progress: Double = 0.0,
    @JvmField var state: SequencerState = SequencerState.IDLE,
    @JvmField @field:JsonIgnoreProperties("camera") val capture: CameraCaptureEvent = CameraCaptureEvent(camera),
) : MessageEvent {

    override val eventName = "SEQUENCER.ELAPSED"
}
