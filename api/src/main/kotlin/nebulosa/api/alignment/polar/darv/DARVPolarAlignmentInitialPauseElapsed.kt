package nebulosa.api.alignment.polar.darv

import com.fasterxml.jackson.annotation.JsonIgnore
import nebulosa.api.sequencer.DelayEvent
import nebulosa.api.services.MessageEvent
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.guide.GuideOutput

data class DARVPolarAlignmentInitialPauseElapsed(
    override val camera: Camera,
    override val guideOutput: GuideOutput,
    val pauseTime: Long,
    val remainingTime: Long,
    val progress: Double,
) : MessageEvent, DARVPolarAlignmentEvent {

    constructor(camera: Camera, guideOutput: GuideOutput, delay: DelayEvent) : this(
        camera, guideOutput, delay.waitTime.inWholeMicroseconds,
        delay.remainingTime.inWholeMicroseconds, delay.progress
    )

    @JsonIgnore override val eventName = "DARV_POLAR_ALIGNMENT_INITIAL_PAUSE_ELAPSED"
}
