package nebulosa.api.alignment.polar.darv

import nebulosa.batch.processing.JobExecution

data class DARVPolarAlignmentFinished(
    override val jobExecution: JobExecution,
) : DARVPolarAlignmentEvent {

    override val state = DARVPolarAlignmentState.IDLE

    override val eventName = "DARV_POLAR_ALIGNMENT_FINISHED"
}
