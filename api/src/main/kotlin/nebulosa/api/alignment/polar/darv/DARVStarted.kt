package nebulosa.api.alignment.polar.darv

import nebulosa.guiding.GuideDirection
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.guide.GuideOutput
import java.time.Duration

data class DARVStarted(
    override val camera: Camera,
    override val guideOutput: GuideOutput,
    override val remainingTime: Duration,
    override val direction: GuideDirection,
) : DARVEvent {

    override val progress = 0.0
    override val state = DARVState.INITIAL_PAUSE
}
