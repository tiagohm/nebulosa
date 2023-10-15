package nebulosa.api.alignment.polar.darv

import com.fasterxml.jackson.annotation.JsonIgnore
import nebulosa.api.services.MessageEvent
import nebulosa.guiding.GuideDirection
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.guide.GuideOutput

data class DARVPolarAlignmentForwardElapsed(
    val camera: Camera,
    val guideOutput: GuideOutput,
    val direction: GuideDirection,
    val remainingTime: Long,
    val progress: Double,
) : MessageEvent {

    @JsonIgnore override val eventName = "DARV_POLAR_ALIGNMENT_FORWARD_ELAPSED"
}
