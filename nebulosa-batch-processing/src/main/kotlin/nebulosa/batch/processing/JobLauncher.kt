package nebulosa.batch.processing

interface JobLauncher : Collection<JobExecution>, Stoppable {

    val stepHandler: StepHandler

    val stepInterceptors: Collection<StepInterceptor>

    fun registerJobListener(listener: JobExecutionListener)

    fun unregisterJobListener(listener: JobExecutionListener)

    fun registerStepListener(listener: StepExecutionListener)

    fun unregisterStepListener(listener: StepExecutionListener)

    fun registerStepInterceptor(interceptor: StepInterceptor)

    fun unregisterStepInterceptor(interceptor: StepInterceptor)

    fun fireBeforeStep(stepExecution: StepExecution)

    fun fireAfterStep(stepExecution: StepExecution)

    fun launch(job: Job, executionContext: ExecutionContext? = null): JobExecution
}
