package nebulosa.batch.processing

interface StepHandler {

    fun handle(step: Step, stepExecution: StepExecution): StepResult
}
