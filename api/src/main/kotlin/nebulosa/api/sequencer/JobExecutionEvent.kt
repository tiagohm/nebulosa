package nebulosa.api.sequencer

import nebulosa.batch.processing.JobExecution

interface JobExecutionEvent {

    val jobExecution: JobExecution
}
