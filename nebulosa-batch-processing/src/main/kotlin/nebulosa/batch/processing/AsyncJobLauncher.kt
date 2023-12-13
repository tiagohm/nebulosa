package nebulosa.batch.processing

import java.time.LocalDateTime
import java.util.concurrent.ExecutorService

open class AsyncJobLauncher(private val executor: ExecutorService) : JobLauncher {

    private val jobListeners = HashSet<JobExecutionListener>()
    private val stepListeners = HashSet<StepExecutionListener>()
    private val mStepInterceptors = HashSet<StepInterceptor>()
    private val jobs = LinkedHashMap<String, JobExecution>()

    override val stepHandler: StepHandler = DefaultStepHandler

    override val stepInterceptors: Collection<StepInterceptor>
        get() = mStepInterceptors

    override fun registerJobListener(listener: JobExecutionListener) {
        jobListeners.add(listener)
    }

    override fun unregisterJobListener(listener: JobExecutionListener) {
        jobListeners.remove(listener)
    }

    override fun registerStepListener(listener: StepExecutionListener) {
        stepListeners.add(listener)
    }

    override fun unregisterStepListener(listener: StepExecutionListener) {
        stepListeners.remove(listener)
    }

    override fun registerStepInterceptor(interceptor: StepInterceptor) {
        mStepInterceptors.add(interceptor)
    }

    override fun unregisterStepInterceptor(interceptor: StepInterceptor) {
        mStepInterceptors.remove(interceptor)
    }

    override fun fireBeforeStep(stepExecution: StepExecution) {
        stepListeners.forEach { it.beforeStep(stepExecution) }
    }

    override fun fireAfterStep(stepExecution: StepExecution) {
        stepListeners.forEach { it.afterStep(stepExecution) }
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

        jobExecution = JobExecution(job, executionContext ?: ExecutionContext(), this)

        jobs[job.id] = jobExecution

        executor.submit {
            jobExecution.status = BatchStatus.STARTED

            job.beforeJob(jobExecution)
            jobListeners.forEach { it.beforeJob(jobExecution) }

            val stepJobListeners = HashSet<JobExecutionListener>()

            try {
                while (jobExecution.canContinue && job.hasNext(jobExecution)) {
                    val step = job.next(jobExecution)

                    if (step is JobExecutionListener) {
                        if (stepJobListeners.add(step)) {
                            step.beforeJob(jobExecution)
                        }
                    }

                    stepHandler.handle(step, StepExecution(step, jobExecution))
                }

                jobExecution.status = if (jobExecution.isStopping) BatchStatus.STOPPED else BatchStatus.COMPLETED
                jobExecution.complete()
            } catch (e: Throwable) {
                jobExecution.status = BatchStatus.FAILED
                jobExecution.completeExceptionally(e)
            } finally {
                jobExecution.finishedAt = LocalDateTime.now()
            }

            job.afterJob(jobExecution)
            jobListeners.forEach { it.afterJob(jobExecution) }
            stepJobListeners.forEach { it.afterJob(jobExecution) }
        }

        return jobExecution
    }

    override fun stop(mayInterruptIfRunning: Boolean) {
        jobs.forEach { it.value.stop(mayInterruptIfRunning) }
    }
}
