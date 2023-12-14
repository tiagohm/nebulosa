package nebulosa.api.guiding

import nebulosa.batch.processing.StepExecution

fun interface GuidePulseListener {

    fun onGuidePulseElapsed(step: GuidePulseStep, stepExecution: StepExecution)
}
