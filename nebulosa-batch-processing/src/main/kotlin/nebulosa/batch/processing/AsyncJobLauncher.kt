package nebulosa.batch.processing

import java.time.LocalDateTime
import java.util.concurrent.ExecutorService

open class AsyncJobLauncher(private val executor: ExecutorService) : JobLauncher, StepInterceptor {

    private val jobListeners = HashSet<JobExecutionListener>()
    private val stepListeners = HashSet<StepExecutionListener>()
    private val stepInterceptors = HashSet<StepInterceptor>()
    private val jobs = LinkedHashMap<String, JobExecution>()

    fun registerJobListener(listener: JobExecutionListener) {
        jobListeners.add(listener)
    }

    fun unregisterJobListener(listener: JobExecutionListener) {
        jobListeners.remove(listener)
    }

    fun registerStepListener(listener: StepExecutionListener) {
        stepListeners.add(listener)
    }

    fun unregisterStepListener(listener: StepExecutionListener) {
        stepListeners.remove(listener)
    }

    fun registerStepInterceptor(interceptor: StepInterceptor) {
        stepInterceptors.add(interceptor)
    }

    fun unregisterStepInterceptor(interceptor: StepInterceptor) {
        stepInterceptors.remove(interceptor)
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
    override fun launch(job: Job): JobExecution {
        var jobExecution = jobs[job.id]

        if (jobExecution != null) {
            if (!jobExecution.isDone) {
                return jobExecution
            }
        }

        val context = ExecutionContext()
        jobExecution = JobExecution(job, context)

        jobs[job.id] = jobExecution

        executor.submit {
            jobExecution.status = BatchStatus.STARTED

            job.beforeJob(jobExecution)
            jobListeners.forEach { it.beforeJob(jobExecution) }

            val interceptors = ArrayList(stepInterceptors)
            interceptors.add(this)

            val stepJobListeners = HashSet<JobExecutionListener>()

            try {
                while (jobExecution.canContinue && job.hasNext(jobExecution)) {
                    val step = job.next(jobExecution)

                    if (step is JobExecutionListener) {
                        if (stepJobListeners.add(step)) {
                            step.beforeJob(jobExecution)
                        }
                    }

                    if (step is FlowStep) {
                        val flow = object : Step {

                            override fun execute(stepExecution: StepExecution): StepResult {
                                step.toList().parallelStream().forEach { execute(interceptors, stepExecution) }
                                return step.execute(stepExecution)
                            }

                            override fun stop(mayInterruptIfRunning: Boolean) {
                                step.stop(mayInterruptIfRunning)
                            }
                        }

                        execute(interceptors, StepExecution(flow, jobExecution))
                    } else {
                        execute(interceptors, StepExecution(step, jobExecution))
                    }
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

    override fun intercept(chain: StepChain): StepResult {
        return chain.stepExecution.step.execute(chain.stepExecution)
    }

    private fun execute(interceptors: List<StepInterceptor>, stepExecution: StepExecution) {
        val chain = StepInterceptorChain(interceptors, stepExecution)
        var status: RepeatStatus

        do {
            stepListeners.forEach { it.beforeStep(stepExecution) }
            val result = chain.proceed()
            stepListeners.forEach { it.afterStep(stepExecution) }
            status = result.get()
        } while (status == RepeatStatus.CONTINUABLE)

        stepExecution.finishedAt = LocalDateTime.now()
    }
}
