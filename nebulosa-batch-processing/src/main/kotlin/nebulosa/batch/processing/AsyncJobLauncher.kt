package nebulosa.batch.processing

import java.time.LocalDateTime
import java.util.concurrent.Executor

open class AsyncJobLauncher(private val executor: Executor) : JobLauncher, StepInterceptor {

    private val jobListeners = HashSet<JobExecutionListener>()
    private val stepListeners = HashSet<StepExecutionListener>()
    private val stepInterceptors = HashSet<StepInterceptor>()
    private val jobs = LinkedHashMap<String, JobExecution>()

    override var stepHandler: StepHandler = DefaultStepHandler

    override fun registerJobExecutionListener(listener: JobExecutionListener): Boolean {
        return jobListeners.add(listener)
    }

    override fun unregisterJobExecutionListener(listener: JobExecutionListener): Boolean {
        return jobListeners.remove(listener)
    }

    override fun registerStepExecutionListener(listener: StepExecutionListener): Boolean {
        return stepListeners.add(listener)
    }

    override fun unregisterStepExecutionListener(listener: StepExecutionListener): Boolean {
        return stepListeners.remove(listener)
    }

    override fun registerStepInterceptor(interceptor: StepInterceptor): Boolean {
        return stepInterceptors.add(interceptor)
    }

    override fun unregisterStepInterceptor(interceptor: StepInterceptor): Boolean {
        return stepInterceptors.remove(interceptor)
    }

    override val size
        get() = jobs.size

    override fun contains(element: JobExecution): Boolean {
        return jobs.containsValue(element)
    }

    override fun containsAll(elements: Collection<JobExecution>): Boolean {
        return elements.all { it in this }
    }

    override fun isEmpty(): Boolean {
        return jobs.isEmpty()
    }

    override fun iterator(): Iterator<JobExecution> {
        return jobs.values.iterator()
    }

    @Synchronized
    override fun launch(job: Job, executionContext: ExecutionContext?): JobExecution {
        var jobExecution = jobs[job.id]

        if (jobExecution != null) {
            if (!jobExecution.isDone) {
                return jobExecution
            }
        }

        val interceptors = ArrayList<StepInterceptor>(stepInterceptors.size + 1)
        interceptors.addAll(stepInterceptors)
        interceptors.add(this)

        jobExecution = JobExecution(job, executionContext ?: ExecutionContext(), this, interceptors)

        jobs[job.id] = jobExecution

        executor.execute {
            jobExecution.status = JobStatus.STARTED

            job.beforeJob(jobExecution)
            jobListeners.forEach { it.beforeJob(jobExecution) }

            try {
                while (jobExecution.canContinue && job.hasNext(jobExecution)) {
                    val step = job.next(jobExecution)
                    stepHandler.handle(step, StepExecution(step, jobExecution))
                }

                jobExecution.status = if (jobExecution.isStopping) JobStatus.STOPPED else JobStatus.COMPLETED
                jobExecution.complete()
            } catch (e: Throwable) {
                jobExecution.status = JobStatus.FAILED
                jobExecution.completeExceptionally(e)
            } finally {
                jobExecution.finishedAt = LocalDateTime.now()
            }

            job.afterJob(jobExecution)
            jobListeners.forEach { it.afterJob(jobExecution) }
        }

        return jobExecution
    }

    override fun stop(mayInterruptIfRunning: Boolean) {
        jobs.forEach { it.value.stop(mayInterruptIfRunning) }
    }

    override fun intercept(chain: StepChain): StepResult {
        stepListeners.forEach { it.beforeStep(chain.stepExecution) }
        val result = chain.step.execute(chain.stepExecution)
        stepListeners.forEach { it.afterStep(chain.stepExecution) }
        return result
    }

    override fun toString() = "AsyncJobLauncher"
}
