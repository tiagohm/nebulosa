package nebulosa.batch.processing

import nebulosa.log.loggerFor

object DefaultStepHandler : StepHandler {

    @JvmStatic private val LOG = loggerFor<DefaultStepHandler>()

    override fun handle(step: Step, stepExecution: StepExecution): StepResult {
        val jobLauncher = stepExecution.jobExecution.jobLauncher

        if (step is JobExecutionListener) {
            if (jobLauncher.registerJobExecutionListener(step)) {
                step.beforeJob(stepExecution.jobExecution)
            }
        }

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
                var status: RepeatStatus

                LOG.info("step started. step={}, context={}", step, stepExecution.context)

                do {
                    status = chain.proceed().get()
                } while (status == RepeatStatus.CONTINUABLE)

                LOG.info("step finished. step={}, context={}", step, stepExecution.context)
            }
        }

        return StepResult.FINISHED
    }
}
