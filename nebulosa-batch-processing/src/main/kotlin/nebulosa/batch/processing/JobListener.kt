package nebulosa.batch.processing

interface JobListener {

    fun beforeJob(jobExecution: JobExecution)

    fun afterJob(jobExecution: JobExecution)
}
