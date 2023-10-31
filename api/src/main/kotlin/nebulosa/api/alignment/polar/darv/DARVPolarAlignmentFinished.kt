package nebulosa.api.alignment.polar.darv

import nebulosa.api.services.MessageEvent
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.guide.GuideOutput

data class DARVPolarAlignmentFinished(
    override val camera: Camera,
    override val guideOutput: GuideOutput,
) : MessageEvent, DARVPolarAlignmentEvent {

    override val progress = 1.0

    override val state = DARVPolarAlignmentState.IDLE

    override val eventName = "DARV_POLAR_ALIGNMENT_FINISHED"
}
