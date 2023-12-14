package nebulosa.batch.processing

data class StepExecution(
    val step: Step,
    val jobExecution: JobExecution,
) {

    inline val context
        get() = jobExecution.context
}
