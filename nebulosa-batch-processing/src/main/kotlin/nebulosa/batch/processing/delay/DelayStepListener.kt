package nebulosa.batch.processing.delay

import nebulosa.batch.processing.StepExecution

fun interface DelayStepListener {

    fun onDelayElapsed(step: DelayStep, stepExecution: StepExecution)
}
