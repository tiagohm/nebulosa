package nebulosa.api.sequencer

import org.springframework.batch.core.StepExecution

interface SequenceStepEvent : SequenceJobEvent {

    val stepExecution: StepExecution

    override val jobExecution
        get() = stepExecution.jobExecution
}
