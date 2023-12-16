package nebulosa.batch.processing

import nebulosa.log.debug
import nebulosa.log.loggerFor

object DefaultStepHandler : StepHandler {

    @JvmStatic private val LOG = loggerFor<DefaultStepHandler>()

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
                val chain = StepInterceptorChain(stepExecution.jobExecution.stepInterceptors, step, stepExecution)

                LOG.debug { "step started. step=%s, context=%s".format(step, stepExecution.context) }

                while (stepExecution.jobExecution.canContinue) {
                    val status = chain.proceed().get()
                    if (status != RepeatStatus.CONTINUABLE) break
                }

                LOG.debug { "step finished. step=%s, context=%s".format(step, stepExecution.context) }
            }
        }

        return StepResult.FINISHED
    }
}
