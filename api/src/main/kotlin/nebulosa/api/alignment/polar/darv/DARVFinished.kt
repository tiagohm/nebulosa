package nebulosa.api.alignment.polar.darv

import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.guide.GuideOutput
import java.time.Duration

data class DARVFinished(
    override val camera: Camera,
    override val guideOutput: GuideOutput,
) : DARVEvent {

    override val remainingTime = Duration.ZERO!!
    override val progress = 0.0
    override val state = DARVState.IDLE
    override val direction = null

    override val eventName = "DARV_POLAR_ALIGNMENT_ELAPSED"
}
