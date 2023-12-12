package nebulosa.batch.processing

interface StepChain {

    val stepExecution: StepExecution

    fun proceed(): StepResult
}
