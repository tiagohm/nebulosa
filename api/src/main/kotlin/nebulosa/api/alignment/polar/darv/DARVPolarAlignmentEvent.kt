package nebulosa.api.alignment.polar.darv

import nebulosa.api.services.MessageEvent
import nebulosa.batch.processing.JobExecution

sealed interface DARVPolarAlignmentEvent : MessageEvent {

    val jobExecution: JobExecution

    val state: DARVPolarAlignmentState
}
