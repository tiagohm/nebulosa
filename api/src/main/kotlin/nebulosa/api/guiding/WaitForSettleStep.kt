package nebulosa.api.guiding

import nebulosa.batch.processing.Step
import nebulosa.batch.processing.StepExecution
import nebulosa.batch.processing.StepResult
import nebulosa.guiding.Guider

data class WaitForSettleStep(private val guider: Guider) : Step {

    override fun execute(stepExecution: StepExecution): StepResult {
        if (guider.isSettling) {
            guider.waitForSettle()
        }

        return StepResult.FINISHED
    }
}
