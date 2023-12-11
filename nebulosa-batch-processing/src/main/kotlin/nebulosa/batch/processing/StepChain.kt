package nebulosa.batch.processing

interface StepChain {

    val step: Step

    val jobExecution: JobExecution

    fun proceed(): StepResult
}
