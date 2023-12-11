package nebulosa.batch.processing

import java.time.LocalDateTime
import java.util.concurrent.ExecutorService

open class AsyncJobLauncher(private val executor: ExecutorService) : JobLauncher, StepInterceptor {

    private val jobListeners = HashSet<JobListener>()
    private val stepListeners = HashSet<StepListener>()
    private val stepInterceptors = HashSet<StepInterceptor>()
    private val jobs = LinkedHashMap<String, JobExecution>()

    fun registerJobListener(listener: JobListener) {
        jobListeners.add(listener)
    }

    fun unregisterJobListener(listener: JobListener) {
        jobListeners.remove(listener)
    }

    fun registerStepListener(listener: StepListener) {
        stepListeners.add(listener)
    }

    fun unregisterStepListener(listener: StepListener) {
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

            try {
                while (jobExecution.canContinue && job.hasNext(jobExecution)) {
                    val step = job.next(jobExecution)

                    if (step is FlowStep) {
                        val flow = object : Step {

                            override fun execute(jobExecution: JobExecution): StepResult {
                                step.toList().parallelStream().forEach { execute(interceptors, it, jobExecution) }
                                return step.execute(jobExecution)
                            }

                            override fun stop(mayInterruptIfRunning: Boolean) {
                                step.stop(mayInterruptIfRunning)
                            }
                        }

                        execute(interceptors, flow, jobExecution)
                    } else {
                        execute(interceptors, step, jobExecution)
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
        }

        return jobExecution
    }

    override fun stop(mayInterruptIfRunning: Boolean) {
        jobs.forEach { it.value.stop(mayInterruptIfRunning) }
    }

    override fun intercept(chain: StepChain): StepResult {
        return chain.step.execute(chain.jobExecution)
    }

    private fun execute(interceptors: List<StepInterceptor>, step: Step, jobExecution: JobExecution) {
        val chain = StepInterceptorChain(interceptors, jobExecution, step)
        var status: RepeatStatus

        do {
            stepListeners.forEach { it.beforeStep(step, jobExecution) }
            val result = chain.proceed()
            stepListeners.forEach { it.afterStep(step, jobExecution) }
            status = result.get()
        } while (status == RepeatStatus.CONTINUABLE)
    }
}
