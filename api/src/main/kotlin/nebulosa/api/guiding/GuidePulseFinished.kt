package nebulosa.api.guiding

import com.fasterxml.jackson.annotation.JsonIgnore
import nebulosa.api.sequencer.SequenceStepEvent
import org.springframework.batch.core.StepExecution

data class GuidePulseFinished(
    @JsonIgnore override val stepExecution: StepExecution,
    @JsonIgnore override val tasklet: GuidePulseTasklet,
) : GuidePulseEvent, SequenceStepEvent {

    @JsonIgnore override val eventName = "GUIDE_PULSE_FINISHED"
}
