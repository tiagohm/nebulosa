package nebulosa.api.cameras

import nebulosa.indi.device.camera.Camera
import nebulosa.log.loggerFor
import org.springframework.batch.core.StepContribution
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.core.step.tasklet.StoppableTasklet
import org.springframework.batch.repeat.RepeatStatus
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.min
import kotlin.system.measureTimeMillis
import kotlin.time.Duration

data class CameraDelayTasklet(
    private val camera: Camera,
    private val exposureDelay: Duration,
    private val listener: CameraCaptureEventListener? = null,
) : StoppableTasklet {

    private val forceAbort = AtomicBoolean()

    override fun execute(contribution: StepContribution, chunkContext: ChunkContext): RepeatStatus {
        val exposureDelayInMilliseconds = contribution.stepExecution.executionContext
            .getLong("exposureDelayInMilliseconds", exposureDelay.inWholeMilliseconds)

        if (exposureDelayInMilliseconds in DELAY_INTERVAL..60000) {
            waitFor(exposureDelayInMilliseconds, forceAbort) {
                if (listener != null) {
                    val progress = if (it > 0) 1.0 - exposureDelayInMilliseconds.toDouble() / it else 1.0
                    val event = CameraDelayUpdated(camera, progress, it * 1000L, exposureDelayInMilliseconds * 1000L)
                    listener.onCameraCaptureEvent(event)
                }
            }
        }

        return RepeatStatus.FINISHED
    }

    override fun stop() {
        LOG.info("stopping delay. camera=${camera.name}")
        forceAbort.set(true)
    }

    companion object {

        const val DELAY_INTERVAL = 500L

        @JvmStatic private val LOG = loggerFor<CameraDelayTasklet>()

        private inline fun waitFor(delay: Long, abort: AtomicBoolean, afterDelay: (Long) -> Unit = {}) {
            var remainingTime = delay

            while (!abort.get() && remainingTime > 0L) {
                remainingTime -= measureTimeMillis { afterDelay(remainingTime) }
                val waitTime = min(remainingTime, DELAY_INTERVAL)

                if (waitTime > 0) {
                    Thread.sleep(waitTime)
                    remainingTime -= waitTime
                }
            }

            afterDelay(0L)
        }
    }
}
