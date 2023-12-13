package nebulosa.batch.processing

data class StepInterceptorChain(
    private val interceptors: List<StepInterceptor>,
    override val step: Step,
    override val stepExecution: StepExecution,
    private val index: Int = 0,
) : StepChain {

    override fun proceed(): StepResult {
        val next = StepInterceptorChain(interceptors, step, stepExecution, index + 1)
        val interceptor = interceptors[index]
        return interceptor.intercept(next)
    }
}
