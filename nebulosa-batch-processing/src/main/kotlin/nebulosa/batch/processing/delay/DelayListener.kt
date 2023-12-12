package nebulosa.batch.processing.delay

import nebulosa.batch.processing.StepExecution

fun interface DelayListener {

    fun onDelayElapsed(stepExecution: StepExecution)
}
