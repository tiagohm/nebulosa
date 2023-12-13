package nebulosa.api.alignment.polar.darv

import nebulosa.batch.processing.JobExecution

data class DARVPolarAlignmentStarted(
    override val jobExecution: JobExecution,
) : DARVPolarAlignmentEvent {

    override val state = DARVPolarAlignmentState.INITIAL_PAUSE

    override val eventName = "DARV_POLAR_ALIGNMENT_STARTED"
}
