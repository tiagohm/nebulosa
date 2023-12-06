package nebulosa.api.guiding

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.batch.core.StepExecution

data class GuidePulseStarted(
    @JsonIgnore override val stepExecution: StepExecution,
    @JsonIgnore override val tasklet: GuidePulseTasklet,
) : GuidePulseEvent {

    override val progress = 0.0
}
