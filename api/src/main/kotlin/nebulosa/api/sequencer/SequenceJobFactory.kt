package nebulosa.api.sequencer

import io.reactivex.rxjava3.functions.Consumer
import nebulosa.api.cameras.CameraCaptureEvent
import nebulosa.api.cameras.CameraStartCaptureRequest
import nebulosa.common.concurrency.Incrementer
import org.springframework.batch.core.Job
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Scope

@Configuration
class SequenceJobFactory(
    private val jobRepository: JobRepository,
    private val sequenceFlowStepFactory: SequenceFlowStepFactory,
    private val sequenceStepFactory: SequenceStepFactory,
    private val sequenceTaskletFactory: SequenceTaskletFactory,
    private val jobIncrementer: Incrementer,
) {

    @Bean(name = ["cameraLoopCaptureJob"], autowireCandidate = false)
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    fun cameraLoopCapture(
        request: CameraStartCaptureRequest,
        cameraCaptureListener: Consumer<CameraCaptureEvent>,
    ): Job {
        val cameraExposureTasklet = sequenceTaskletFactory.cameraLoopExposure(request)
        cameraExposureTasklet.subscribe(cameraCaptureListener)

        val cameraExposureStep = sequenceStepFactory.cameraExposure(cameraExposureTasklet)

        return JobBuilder("CameraCapture.Job.${jobIncrementer.increment()}", jobRepository)
            .start(cameraExposureStep)
            .listener(cameraExposureTasklet)
            .build()
    }

    @Bean(name = ["cameraCaptureJob"], autowireCandidate = false)
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    fun cameraCapture(
        request: CameraStartCaptureRequest,
        cameraCaptureListener: Consumer<CameraCaptureEvent>,
    ): Job {
        val cameraExposureTasklet = sequenceTaskletFactory.cameraExposure(request)
        cameraExposureTasklet.subscribe(cameraCaptureListener)

        val cameraDelayTasklet = sequenceTaskletFactory.delay(request.exposureDelay)
        cameraDelayTasklet.subscribe(cameraExposureTasklet)

        val ditherTasklet = sequenceTaskletFactory.ditherAfterExposure(request.dither)
        val waitForSettleTasklet = sequenceTaskletFactory.waitForSettle()

        val jobBuilder = JobBuilder("CameraCapture.Job.${jobIncrementer.increment()}", jobRepository)
            .start(sequenceStepFactory.waitForSettle(waitForSettleTasklet))
            .next(sequenceStepFactory.cameraExposure(cameraExposureTasklet))

        repeat(request.exposureAmount - 1) {
            jobBuilder.next(sequenceFlowStepFactory.delayAndWaitForSettle(cameraDelayTasklet, waitForSettleTasklet))
                .next(sequenceStepFactory.cameraExposure(cameraExposureTasklet))
                .next(sequenceStepFactory.dither(ditherTasklet))
        }

        return jobBuilder
            .listener(cameraExposureTasklet)
            .listener(cameraDelayTasklet)
            .build()
    }
}
