package nebulosa.api.alignment.polar.darv

import com.fasterxml.jackson.annotation.JsonIgnore
import nebulosa.api.services.MessageEvent
import nebulosa.guiding.GuideDirection
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.guide.GuideOutput

data class DARVPolarAlignmentGuidePulseElapsed(
    override val camera: Camera,
    override val guideOutput: GuideOutput,
    val forward: Boolean,
    val direction: GuideDirection,
    val remainingTime: Long,
    val progress: Double,
) : MessageEvent, DARVPolarAlignmentEvent {

    @JsonIgnore override val eventName = "DARV_POLAR_ALIGNMENT_GUIDE_PULSE_ELAPSED"
}
