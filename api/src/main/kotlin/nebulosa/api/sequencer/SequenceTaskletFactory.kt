package nebulosa.api.sequencer

import nebulosa.api.cameras.CameraExposureTasklet
import nebulosa.api.cameras.CameraLoopExposureTasklet
import nebulosa.api.cameras.CameraStartCaptureRequest
import nebulosa.api.guiding.*
import nebulosa.api.sequencer.tasklets.delay.DelayTasklet
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Scope
import kotlin.time.Duration

@Configuration
class SequenceTaskletFactory {

    @Bean(name = ["delayTasklet"], autowireCandidate = false)
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    fun delay(duration: Duration): DelayTasklet {
        return DelayTasklet(duration)
    }

    @Bean(name = ["cameraExposureTasklet"], autowireCandidate = false)
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    fun cameraExposure(request: CameraStartCaptureRequest): CameraExposureTasklet {
        return CameraExposureTasklet(request)
    }

    @Bean(name = ["cameraLoopExposureTasklet"], autowireCandidate = false)
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    fun cameraLoopExposure(request: CameraStartCaptureRequest): CameraLoopExposureTasklet {
        return CameraLoopExposureTasklet(request)
    }

    @Bean(name = ["guidePulseTasklet"], autowireCandidate = false)
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    fun guidePulse(request: GuidePulseRequest): GuidePulseTasklet {
        return GuidePulseTasklet(request)
    }

    @Bean(name = ["ditherTasklet"], autowireCandidate = false)
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    fun ditherAfterExposure(request: DitherAfterExposureRequest): DitherAfterExposureTasklet {
        return DitherAfterExposureTasklet(request)
    }

    @Bean(name = ["waitForSettleTasklet"], autowireCandidate = false)
    @Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
    fun waitForSettle(): WaitForSettleTasklet {
        return WaitForSettleTasklet()
    }
}
