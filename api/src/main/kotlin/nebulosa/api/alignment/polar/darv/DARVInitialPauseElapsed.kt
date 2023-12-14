package nebulosa.api.alignment.polar.darv

import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.guide.GuideOutput
import java.time.Duration

data class DARVInitialPauseElapsed(
    override val camera: Camera,
    override val guideOutput: GuideOutput,
    override val remainingTime: Duration,
    override val progress: Double,
) : DARVEvent {

    override val state = DARVState.INITIAL_PAUSE
    override val direction = null

    override val eventName = "DARV_POLAR_ALIGNMENT_ELAPSED"
}
