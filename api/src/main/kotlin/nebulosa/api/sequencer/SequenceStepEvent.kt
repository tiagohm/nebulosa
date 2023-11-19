package nebulosa.api.sequencer

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.batch.core.StepExecution

interface SequenceStepEvent : SequenceJobEvent {

    val stepExecution: StepExecution

    override val jobExecution
        @JsonIgnore get() = stepExecution.jobExecution
}
