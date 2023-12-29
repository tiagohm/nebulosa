package nebulosa.batch.processing

interface StepChain {

    val step: Step

    val stepExecution: StepExecution

    fun proceed(): StepResult
}
