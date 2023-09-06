package nebulosa.api.cameras

import nebulosa.indi.device.camera.Camera
import nebulosa.log.loggerFor
import org.greenrobot.eventbus.EventBus
import org.springframework.batch.core.StepContribution
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.core.step.tasklet.StoppableTasklet
import org.springframework.batch.repeat.RepeatStatus
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.min
import kotlin.system.measureTimeMillis

class CameraDelayTasklet(
    private val camera: Camera,
    private val exposureDelayInSeconds: Long,
) : StoppableTasklet {

    private val forceAbort = AtomicBoolean()

    override fun execute(contribution: StepContribution, chunkContext: ChunkContext): RepeatStatus {
        if (exposureDelayInSeconds in 1..60) {
            val delayInMilliseconds = exposureDelayInSeconds * 1000L

            waitFor(delayInMilliseconds, forceAbort) {
                val progress = if (it > 0) 1.0 - delayInMilliseconds.toDouble() / it else 1.0
                val event = CameraDelayUpdated(camera, contribution.stepExecution.jobExecutionId, progress, it * 1000L)
                EventBus.getDefault().post(event)
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
