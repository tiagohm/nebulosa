package nebulosa.api.alignment.polar.darv

import com.fasterxml.jackson.annotation.JsonIgnore
import nebulosa.api.services.MessageEvent
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.guide.GuideOutput
import org.springframework.batch.core.JobExecution

data class DARVPolarAlignmentFinished(
    override val camera: Camera,
    override val guideOutput: GuideOutput,
    @JsonIgnore override val jobExecution: JobExecution,
) : MessageEvent, DARVPolarAlignmentEvent {

    override val state = DARVPolarAlignmentState.IDLE

    override val eventName = "DARV_POLAR_ALIGNMENT_FINISHED"
}
