package nebulosa.batch.processing

interface JobExecutionListener {

    fun beforeJob(jobExecution: JobExecution) = Unit

    fun afterJob(jobExecution: JobExecution) = Unit
}
