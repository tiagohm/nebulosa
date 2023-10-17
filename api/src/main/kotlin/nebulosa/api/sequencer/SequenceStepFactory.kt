package nebulosa.api.sequencer

import nebulosa.api.cameras.CameraStartCaptureTasklet
import nebulosa.api.guiding.DitherAfterExposureTasklet
import nebulosa.api.guiding.GuidePulseTasklet
import nebulosa.api.guiding.WaitForSettleTasklet
import nebulosa.api.sequencer.tasklets.delay.DelayTasklet
import nebulosa.common.concurrency.Incrementer
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.core.step.tasklet.TaskletStep
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Scope
import org.springframework.transaction.PlatformTransactionManager

@Configuration
class SequenceStepFactory(
    private val jobRepository: JobRepository,
    private val platformTransactionManager: PlatformTransactionManager,
    private val stepIncrementer: Incrementer,
) {

    @Bean(name = ["delayStep"], autowireCandidate = false)
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    fun delay(delayTasklet: DelayTasklet): TaskletStep {
        return StepBuilder("Step.Delay.${stepIncrementer.increment()}", jobRepository)
            .tasklet(delayTasklet, platformTransactionManager)
            .build()
    }

    @Bean(name = ["cameraExposureStep"], autowireCandidate = false)
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    fun cameraExposure(cameraExposureTasklet: CameraStartCaptureTasklet): TaskletStep {
        return StepBuilder("Step.Exposure.${stepIncrementer.increment()}", jobRepository)
            .tasklet(cameraExposureTasklet, platformTransactionManager)
            .build()
    }

    @Bean(name = ["guidePulseStep"], autowireCandidate = false)
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    fun guidePulse(guidePulseTasklet: GuidePulseTasklet): TaskletStep {
        return StepBuilder("Step.GuidePulse.${stepIncrementer.increment()}", jobRepository)
            .tasklet(guidePulseTasklet, platformTransactionManager)
            .build()
    }

    @Bean(name = ["ditherStep"], autowireCandidate = false)
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    fun dither(ditherAfterExposureTasklet: DitherAfterExposureTasklet): TaskletStep {
        return StepBuilder("Step.DitherAfterExposure.${stepIncrementer.increment()}", jobRepository)
            .tasklet(ditherAfterExposureTasklet, platformTransactionManager)
            .build()
    }

    @Bean(name = ["waitForSettleStep"], autowireCandidate = false)
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    fun waitForSettle(waitForSettleTasklet: WaitForSettleTasklet): TaskletStep {
        return StepBuilder("Step.WaitForSettle.${stepIncrementer.increment()}", jobRepository)
            .tasklet(waitForSettleTasklet, platformTransactionManager)
            .build()
    }
}
