package nebulosa.api.guiding

import com.fasterxml.jackson.annotation.JsonIgnore
import nebulosa.api.sequencer.SequenceStepEvent
import nebulosa.guiding.GuideDirection
import org.springframework.batch.core.StepExecution

data class GuidePulseElapsed(
    val remainingTime: Long,
    val progress: Double,
    val direction: GuideDirection,
    @JsonIgnore override val stepExecution: StepExecution,
    @JsonIgnore override val tasklet: GuidePulseTasklet,
) : GuidePulseEvent, SequenceStepEvent {

    @JsonIgnore override val eventName = "GUIDE_PULSE_ELAPSED"
}
