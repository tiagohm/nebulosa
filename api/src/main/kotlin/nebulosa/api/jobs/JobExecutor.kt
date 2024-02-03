package nebulosa.api.jobs

import nebulosa.batch.processing.Job
import nebulosa.batch.processing.JobExecution
import nebulosa.batch.processing.JobLauncher
import java.util.*

abstract class JobExecutor {

    protected abstract val jobLauncher: JobLauncher

    @PublishedApi internal val jobExecutions = LinkedList<JobExecution>()

    protected fun register(jobExecution: JobExecution) {
        jobExecutions.add(jobExecution)
    }

    protected inline fun findJobExecutionWith(test: Job.() -> Boolean): JobExecution? {
        for (i in jobExecutions.indices.reversed()) {
            val jobExecution = jobExecutions[i]
            val job = jobExecution.job

            if (!jobExecution.isDone && job.test()) {
                return jobExecution
            }
        }

        return null
    }

    protected fun findJobExecutionWithAll(vararg data: Any): JobExecution? {
        return findJobExecutionWith { data.all { it in this } }
    }

    protected fun findJobExecutionWithAny(vararg data: Any): JobExecution? {
        return findJobExecutionWith { data.any { it in this } }
    }

    fun findJobExecution(id: String): JobExecution? {
        return jobExecutions.find { it.job.id == id }
    }

    @Synchronized
    protected fun stopWithAll(vararg data: Any) {
        val jobExecution = findJobExecutionWithAll(*data) ?: return
        jobLauncher.stop(jobExecution)
    }

    @Synchronized
    protected fun stopWithAny(vararg data: Any) {
        val jobExecution = findJobExecutionWithAny(*data) ?: return
        jobLauncher.stop(jobExecution)
    }

    @Synchronized
    fun stop(id: String) {
        val jobExecution = findJobExecution(id) ?: return
        jobLauncher.stop(jobExecution)
    }

    fun isRunning(id: String): Boolean {
        return findJobExecution(id) != null
    }
}
