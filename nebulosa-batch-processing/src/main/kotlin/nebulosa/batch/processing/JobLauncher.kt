package nebulosa.batch.processing

interface JobLauncher : Collection<JobExecution>, Stoppable {

    val stepHandler: StepHandler

    fun registerJobExecutionListener(listener: JobExecutionListener): Boolean

    fun unregisterJobExecutionListener(listener: JobExecutionListener): Boolean

    fun registerStepExecutionListener(listener: StepExecutionListener): Boolean

    fun unregisterStepExecutionListener(listener: StepExecutionListener): Boolean

    fun registerStepInterceptor(interceptor: StepInterceptor): Boolean

    fun unregisterStepInterceptor(interceptor: StepInterceptor): Boolean

    fun launch(job: Job, executionContext: ExecutionContext? = null): JobExecution

    fun stop(jobExecution: JobExecution, mayInterruptIfRunning: Boolean = true)
}
