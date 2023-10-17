package nebulosa.api.sequencer

import nebulosa.api.cameras.CameraExposureTasklet
import nebulosa.api.guiding.GuidePulseTasklet
import nebulosa.api.guiding.WaitForSettleTasklet
import nebulosa.api.sequencer.tasklets.delay.DelayTasklet
import nebulosa.common.concurrency.Incrementer
import org.springframework.batch.core.job.builder.FlowBuilder
import org.springframework.batch.core.job.flow.support.SimpleFlow
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Scope
import org.springframework.core.task.SimpleAsyncTaskExecutor

@Configuration
class SequenceFlowFactory(
    private val flowIncrementer: Incrementer,
    private val sequenceStepFactory: SequenceStepFactory,
    private val simpleAsyncTaskExecutor: SimpleAsyncTaskExecutor,
) {

    @Bean(name = ["cameraExposureFlow"], autowireCandidate = false)
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    fun cameraExposure(cameraExposureTasklet: CameraExposureTasklet): SimpleFlow {
        val step = sequenceStepFactory.cameraExposure(cameraExposureTasklet)
        return FlowBuilder<SimpleFlow>("Flow.CameraExposure.${flowIncrementer.increment()}").start(step).end()
    }

    @Bean(name = ["delayFlow"], autowireCandidate = false)
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    fun delay(delayTasklet: DelayTasklet): SimpleFlow {
        val step = sequenceStepFactory.delay(delayTasklet)
        return FlowBuilder<SimpleFlow>("Flow.Delay.${flowIncrementer.increment()}").start(step).end()
    }

    @Bean(name = ["waitForSettleFlow"], autowireCandidate = false)
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    fun waitForSettle(waitForSettleTasklet: WaitForSettleTasklet): SimpleFlow {
        val step = sequenceStepFactory.waitForSettle(waitForSettleTasklet)
        return FlowBuilder<SimpleFlow>("Flow.WaitForSettle.${flowIncrementer.increment()}").start(step).end()
    }

    @Bean(name = ["guidePulseFlow"], autowireCandidate = false)
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    fun guidePulse(
        initialPauseDelayTasklet: DelayTasklet,
        forwardGuidePulseTasklet: GuidePulseTasklet, backwardGuidePulseTasklet: GuidePulseTasklet
    ): SimpleFlow {
        return FlowBuilder<SimpleFlow>("Flow.GuidePulse.${flowIncrementer.increment()}")
            .start(sequenceStepFactory.delay(initialPauseDelayTasklet))
            .next(sequenceStepFactory.guidePulse(forwardGuidePulseTasklet))
            .next(sequenceStepFactory.guidePulse(backwardGuidePulseTasklet))
            .end()
    }

    @Bean(name = ["delayAndWaitForSettleFlow"], autowireCandidate = false)
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    fun delayAndWaitForSettle(cameraDelayTasklet: DelayTasklet, waitForSettleTasklet: WaitForSettleTasklet): SimpleFlow {
        return FlowBuilder<SimpleFlow>("Flow.DelayAndWaitForSettle.${flowIncrementer.increment()}")
            .start(delay(cameraDelayTasklet))
            .split(simpleAsyncTaskExecutor)
            .add(waitForSettle(waitForSettleTasklet))
            .end()
    }
}
