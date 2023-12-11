package nebulosa.batch.processing

data class StepInterceptorChain(
    private val interceptors: List<StepInterceptor>,
    override val jobExecution: JobExecution,
    override val step: Step,
    private val index: Int = 0,
) : StepChain {

    override fun proceed(): StepResult {
        val next = StepInterceptorChain(interceptors, jobExecution, step, index + 1)
        val interceptor = interceptors[index]
        return interceptor.intercept(next)
    }
}
