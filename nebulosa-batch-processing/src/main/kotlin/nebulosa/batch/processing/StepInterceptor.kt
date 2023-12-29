package nebulosa.batch.processing

interface StepInterceptor {

    fun intercept(chain: StepChain): StepResult
}
