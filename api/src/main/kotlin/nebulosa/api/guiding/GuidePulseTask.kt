package nebulosa.api.guiding

import nebulosa.guiding.GuideDirection
import nebulosa.indi.device.guider.GuideOutput
import nebulosa.job.manager.Job
import nebulosa.job.manager.Task
import nebulosa.job.manager.delay.DelayTask
import nebulosa.log.d
import nebulosa.log.loggerFor
import nebulosa.util.concurrency.cancellation.CancellationSource
import java.time.Duration

data class GuidePulseTask(
    @JvmField val job: Job,
    @JvmField val guideOutput: GuideOutput,
    @JvmField val request: GuidePulseRequest,
) : Task {

    private val direction = request.direction
    private val duration = request.duration.toMillis()

    @JvmField val delayTask = DelayTask(job, duration)

    override fun run() {
        if (!job.isCancelled && guideOutput.pulseGuide(request.duration, request.direction)) {
            LOG.d { debug("Guide Pulse started. guideOutput={}, duration={} ms, direction={}", guideOutput, guideOutput, direction) }
            delayTask.run()
            LOG.d { debug("Guide Pulse finished. guideOutput={}, duration={} ms, direction={}", guideOutput, guideOutput, direction) }
        }
    }

    override fun onCancel(source: CancellationSource) {
        delayTask.onCancel(source)
        guideOutput.pulseGuide(Duration.ZERO, request.direction)
    }

    override fun onPause(paused: Boolean) {
        delayTask.onPause(paused)
    }

    companion object {

        private val LOG = loggerFor<GuidePulseTask>()

        internal fun GuideOutput.pulseGuide(duration: Duration, direction: GuideDirection): Boolean {
            when (direction) {
                GuideDirection.NORTH -> guideNorth(duration)
                GuideDirection.SOUTH -> guideSouth(duration)
                GuideDirection.WEST -> guideWest(duration)
                GuideDirection.EAST -> guideEast(duration)
                else -> return false
            }

            return true
        }
    }
}
