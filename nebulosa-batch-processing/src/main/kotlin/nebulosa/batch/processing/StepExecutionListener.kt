package nebulosa.batch.processing

interface StepExecutionListener {

    fun beforeStep(stepExecution: StepExecution) = Unit

    fun afterStep(stepExecution: StepExecution) = Unit
}
