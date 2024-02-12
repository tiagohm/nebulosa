package nebulosa.batch.processing

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

    protected fun findJobExecutionWithAny(vararg data: Any): JobExecution? {
        return findJobExecutionWith { data.any { it in this } }
    }

    fun findJobExecution(id: String): JobExecution? {
        return jobExecutions.find { it.job.id == id }
    }

    fun stop(id: String) {
        val jobExecution = findJobExecution(id) ?: return
        jobLauncher.stop(jobExecution)
    }

    fun pause(id: String) {
        val jobExecution = findJobExecution(id) ?: return
        jobLauncher.pause(jobExecution)
    }

    fun unpause(id: String) {
        val jobExecution = findJobExecution(id) ?: return
        jobLauncher.unpause(jobExecution)
    }

    fun isRunning(id: String): Boolean {
        return findJobExecution(id) != null
    }
}
