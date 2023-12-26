package nebulosa.batch.processing

import nebulosa.common.concurrency.CancellationListener
import nebulosa.common.concurrency.CancellationSource
import nebulosa.log.debug
import nebulosa.log.loggerFor
import java.io.Closeable
import java.time.LocalDateTime
import java.util.concurrent.Executor

open class AsyncJobLauncher(private val executor: Executor) : JobLauncher, StepInterceptor, CancellationListener {

    private val jobListeners = LinkedHashSet<JobExecutionListener>()
    private val stepListeners = LinkedHashSet<StepExecutionListener>()
    private val stepInterceptors = LinkedHashSet<StepInterceptor>()
    private val jobs = LinkedHashSet<JobExecution>()

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
        return element in jobs
    }

    override fun containsAll(elements: Collection<JobExecution>): Boolean {
        return elements.all { it in this }
    }

    override fun isEmpty(): Boolean {
        return jobs.isEmpty()
    }

    override fun iterator(): Iterator<JobExecution> {
        return jobs.iterator()
    }

    @Synchronized
    override fun launch(job: Job, executionContext: ExecutionContext?): JobExecution {
        var jobExecution = jobs.find { it.job === job }

        if (jobExecution != null) {
            if (!jobExecution.isDone || jobExecution.isStopping) {
                LOG.warn("unable to launch new job {}, because it is running or stopping.", job::class.simpleName)
                return jobExecution
            }
        }

        val interceptors = ArrayList<StepInterceptor>(stepInterceptors.size + 1)
        interceptors.addAll(stepInterceptors)
        interceptors.add(this)

        jobExecution = JobExecution(job, executionContext ?: jobExecution?.context ?: ExecutionContext(), this, interceptors)

        jobs.add(jobExecution)

        jobExecution.cancellationToken.listen(this)

        executor.execute {
            jobExecution.status = JobStatus.STARTED

            LOG.debug { "job started. job={}".format(job) }

            job.beforeJob(jobExecution)
            jobListeners.forEach { it.beforeJob(jobExecution) }

            val stepJobListeners = LinkedHashSet<JobExecutionListener>()

            try {
                while (jobExecution.canContinue && job.hasNext(jobExecution)) {
                    val step = job.next(jobExecution)

                    if (stepJobListeners.add(step)) {
                        step.beforeJob(jobExecution)
                    }

                    val result = stepHandler.handle(step, StepExecution(step, jobExecution))
                    result.get()
                }

                jobExecution.status = if (jobExecution.isStopping) JobStatus.STOPPED else JobStatus.COMPLETED
                jobExecution.complete()

                LOG.debug { "job finished. job={}".format(job) }
            } catch (e: Throwable) {
                LOG.error("job failed. job=$job, jobExecution=$jobExecution", e)
                jobExecution.status = JobStatus.FAILED
                jobExecution.completeExceptionally(e)
            } finally {
                jobExecution.finishedAt = LocalDateTime.now()
                jobExecution.cancellationToken.unlisten(this)
            }

            fun JobExecutionListener.afterJob() {
                afterJob(jobExecution)

                if (this is Closeable) {
                    close()
                }
                if (this is PublishSubscribe<*>) {
                    onComplete()
                }
                if (this is MutableCollection<*>) {
                    clear()
                }
            }

            job.afterJob()
            jobListeners.forEach { it.afterJob() }
            stepJobListeners.forEach { it.afterJob() }
        }

        return jobExecution
    }

    override fun stop(mayInterruptIfRunning: Boolean) {
        jobs.forEach { stop(it, mayInterruptIfRunning) }
    }

    override fun stop(jobExecution: JobExecution, mayInterruptIfRunning: Boolean) {
        if (!jobExecution.isDone && !jobExecution.isStopping) {
            jobExecution.status = JobStatus.STOPPING
            jobExecution.job.stop(mayInterruptIfRunning)

            if (!jobExecution.cancellationToken.isDone) {
                jobExecution.cancellationToken.cancel(mayInterruptIfRunning)
            }
        }
    }

    override fun intercept(chain: StepChain): StepResult {
        stepListeners.forEach { it.beforeStep(chain.stepExecution) }
        val result = chain.step.execute(chain.stepExecution)
        stepListeners.forEach { it.afterStep(chain.stepExecution) }
        return result
    }

    override fun accept(source: CancellationSource) {
        if (source is CancellationSource.Cancel) {
            stop(source.mayInterruptIfRunning)
        }
    }

    override fun toString() = "AsyncJobLauncher"

    companion object {

        @JvmStatic private val LOG = loggerFor<AsyncJobLauncher>()
    }
}
