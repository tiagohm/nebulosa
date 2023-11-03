package nebulosa.api.alignment.polar.darv

import com.fasterxml.jackson.annotation.JsonIgnore
import nebulosa.api.services.MessageEvent
import nebulosa.guiding.GuideDirection
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.guide.GuideOutput
import org.springframework.batch.core.JobExecution
import kotlin.time.Duration

data class DARVPolarAlignmentGuidePulseElapsed(
    override val camera: Camera,
    override val guideOutput: GuideOutput,
    override val state: DARVPolarAlignmentState,
    val direction: GuideDirection,
    val remainingTime: Duration,
    override val progress: Double,
    @JsonIgnore override val jobExecution: JobExecution,
) : MessageEvent, DARVPolarAlignmentEvent {

    override val eventName = "DARV_POLAR_ALIGNMENT_UPDATED"
}
