package nebulosa.api.alignment.polar.darv

import nebulosa.api.services.MessageEvent
import nebulosa.batch.processing.JobExecution

data class DARVPolarAlignmentGuidePulseElapsed(
    override val jobExecution: JobExecution,
    override val state: DARVPolarAlignmentState,
) : MessageEvent, DARVPolarAlignmentEvent {

    override val eventName = "DARV_POLAR_ALIGNMENT_UPDATED"
}
