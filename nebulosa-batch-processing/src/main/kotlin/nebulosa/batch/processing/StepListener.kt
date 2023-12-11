package nebulosa.batch.processing

interface StepListener {

    fun beforeStep(step: Step, jobExecution: JobExecution)

    fun afterStep(step: Step, jobExecution: JobExecution)
}
