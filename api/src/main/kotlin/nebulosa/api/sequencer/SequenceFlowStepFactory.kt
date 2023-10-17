package nebulosa.api.sequencer

import nebulosa.api.guiding.WaitForSettleTasklet
import nebulosa.api.sequencer.tasklets.delay.DelayTasklet
import nebulosa.common.concurrency.Incrementer
import org.springframework.batch.core.Step
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Scope

@Configuration
class SequenceFlowStepFactory(
    private val jobRepository: JobRepository,
    private val stepIncrementer: Incrementer,
    private val sequenceFlowFactory: SequenceFlowFactory,
) {

    @Bean(name = ["delayAndWaitForSettleFlowStep"], autowireCandidate = false)
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    fun delayAndWaitForSettle(cameraDelayTasklet: DelayTasklet, waitForSettleTasklet: WaitForSettleTasklet): Step {
        return StepBuilder("FlowStep.DelayAndWaitForSettle.${stepIncrementer.increment()}", jobRepository)
            .flow(sequenceFlowFactory.delayAndWaitForSettle(cameraDelayTasklet, waitForSettleTasklet))
            .build()
    }
}
