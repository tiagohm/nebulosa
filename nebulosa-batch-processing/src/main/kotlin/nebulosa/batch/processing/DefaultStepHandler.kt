package nebulosa.batch.processing

import java.time.LocalDateTime

object DefaultStepHandler : StepHandler, StepInterceptor {

    override fun handle(step: Step, stepExecution: StepExecution): StepResult {
        val jobLauncher = stepExecution.jobExecution.jobLauncher

        when (step) {
            is SplitStep -> {
                step.beforeStep(stepExecution)
                step.parallelStream().forEach { jobLauncher.stepHandler.handle(it, stepExecution) }
                step.afterStep(stepExecution)
            }
            is FlowStep -> {
                step.beforeStep(stepExecution)
                step.forEach { jobLauncher.stepHandler.handle(it, stepExecution) }
                step.afterStep(stepExecution)
            }
            else -> {
                val interceptors = ArrayList<StepInterceptor>(jobLauncher.stepInterceptors.size + 1)
                interceptors.addAll(jobLauncher.stepInterceptors)
                interceptors.add(this)

                val chain = StepInterceptorChain(interceptors, step, stepExecution)
                var status: RepeatStatus

                do {
                    jobLauncher.fireBeforeStep(stepExecution)
                    val result = chain.proceed()
                    jobLauncher.fireAfterStep(stepExecution)
                    status = result.get()
                } while (status == RepeatStatus.CONTINUABLE)

                stepExecution.finishedAt = LocalDateTime.now()
            }
        }

        return StepResult.FINISHED
    }

    override fun intercept(chain: StepChain): StepResult {
        return chain.step.execute(chain.stepExecution)
    }
}
