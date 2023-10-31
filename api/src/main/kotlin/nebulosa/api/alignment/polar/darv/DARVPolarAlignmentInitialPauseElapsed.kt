package nebulosa.api.alignment.polar.darv

import nebulosa.api.sequencer.DelayEvent
import nebulosa.api.services.MessageEvent
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.guide.GuideOutput

data class DARVPolarAlignmentInitialPauseElapsed(
    override val camera: Camera,
    override val guideOutput: GuideOutput,
    val pauseTime: Long,
    val remainingTime: Long,
    override val progress: Double,
) : MessageEvent, DARVPolarAlignmentEvent {

    constructor(camera: Camera, guideOutput: GuideOutput, delay: DelayEvent) : this(
        camera, guideOutput, delay.waitTime.inWholeMicroseconds,
        delay.remainingTime.inWholeMicroseconds, delay.progress
    )

    override val state = DARVPolarAlignmentState.INITIAL_PAUSE

    override val eventName = "DARV_POLAR_ALIGNMENT_UPDATED"
}
