package nebulosa.api.alignment.polar.darv

import com.fasterxml.jackson.annotation.JsonIgnore
import nebulosa.api.sequencer.tasklets.delay.DelayEvent
import nebulosa.api.services.MessageEvent
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.guide.GuideOutput
import org.springframework.batch.core.JobExecution
import java.time.Duration

data class DARVPolarAlignmentInitialPauseElapsed(
    override val camera: Camera,
    override val guideOutput: GuideOutput,
    val pauseTime: Duration,
    val remainingTime: Duration,
    override val progress: Double,
    @JsonIgnore override val jobExecution: JobExecution,
) : MessageEvent, DARVPolarAlignmentEvent {

    constructor(camera: Camera, guideOutput: GuideOutput, delay: DelayEvent) : this(
        camera, guideOutput, delay.tasklet.duration,
        delay.remainingTime, delay.progress, delay.jobExecution
    )

    override val state = DARVPolarAlignmentState.INITIAL_PAUSE

    override val eventName = "DARV_POLAR_ALIGNMENT_UPDATED"
}
