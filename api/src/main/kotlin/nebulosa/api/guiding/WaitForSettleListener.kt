package nebulosa.api.guiding

import nebulosa.batch.processing.StepExecution

interface WaitForSettleListener {

    fun onSettleStarted(step: WaitForSettleStep, stepExecution: StepExecution)

    fun onSettleFinished(step: WaitForSettleStep, stepExecution: StepExecution)
}
