package nebulosa.batch.processing

interface Job : JobExecutionListener, Stoppable {

    fun hasNext(jobExecution: JobExecution): Boolean

    fun next(jobExecution: JobExecution): Step

    override fun beforeJob(jobExecution: JobExecution) = Unit

    override fun afterJob(jobExecution: JobExecution) = Unit
}
