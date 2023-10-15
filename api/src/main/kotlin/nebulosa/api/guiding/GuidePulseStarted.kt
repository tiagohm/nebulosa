package nebulosa.api.guiding

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.batch.core.StepExecution

data class GuidePulseStarted(
    @JsonIgnore override val stepExecution: StepExecution,
    @JsonIgnore override val tasklet: GuidePulseTasklet,
) : GuidePulseEvent {

    @JsonIgnore override val eventName = "GUIDE_PULSE_STARTED"
}
