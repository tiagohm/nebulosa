package nebulosa.api.sequencer

import org.springframework.batch.core.JobExecution

interface SequenceJobEvent {

    val jobExecution: JobExecution
}
