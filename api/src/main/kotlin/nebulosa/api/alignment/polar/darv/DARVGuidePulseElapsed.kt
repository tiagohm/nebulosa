package nebulosa.api.alignment.polar.darv

import nebulosa.api.messages.MessageEvent
import nebulosa.guiding.GuideDirection
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.guide.GuideOutput
import java.time.Duration

data class DARVGuidePulseElapsed(
    override val camera: Camera,
    override val guideOutput: GuideOutput,
    override val remainingTime: Duration,
    override val progress: Double,
    override val direction: GuideDirection,
    override val state: DARVState,
) : MessageEvent, DARVEvent
