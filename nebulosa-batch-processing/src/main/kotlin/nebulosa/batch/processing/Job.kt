package nebulosa.batch.processing

interface Job : JobExecutionListener, Stoppable {

    val id: String

    fun hasNext(jobExecution: JobExecution): Boolean

    fun next(jobExecution: JobExecution): Step

    override fun beforeJob(jobExecution: JobExecution) = Unit

    override fun afterJob(jobExecution: JobExecution) = Unit

    operator fun contains(data: Any): Boolean
}
