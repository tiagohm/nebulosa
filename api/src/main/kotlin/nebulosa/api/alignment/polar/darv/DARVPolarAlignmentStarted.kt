package nebulosa.api.alignment.polar.darv

import com.fasterxml.jackson.annotation.JsonIgnore
import nebulosa.api.services.MessageEvent
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.guide.GuideOutput

data class DARVPolarAlignmentStarted(
    override val camera: Camera,
    override val guideOutput: GuideOutput,
) : MessageEvent, DARVPolarAlignmentEvent {

    @JsonIgnore override val eventName = "DARV_POLAR_ALIGNMENT_STARTED"
}
