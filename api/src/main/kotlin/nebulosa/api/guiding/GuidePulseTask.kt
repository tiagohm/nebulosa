package nebulosa.api.guiding

import nebulosa.guiding.GuideDirection
import nebulosa.indi.device.guider.GuideOutput
import nebulosa.job.manager.Job
import nebulosa.job.manager.Task
import nebulosa.job.manager.delay.DelayTask
import nebulosa.log.debug
import nebulosa.log.loggerFor
import nebulosa.util.concurrency.cancellation.CancellationSource
import java.time.Duration

data class GuidePulseTask(
    @JvmField val guideOutput: GuideOutput,
    @JvmField val request: GuidePulseRequest,
) : Task {

    private val direction = request.direction
    private val duration = request.duration.toMillis()

    @JvmField val delayTask = DelayTask(duration)

    override fun execute(job: Job) {
        if (!job.isCancelled && guideOutput.pulseGuide(request.duration, request.direction)) {
            LOG.debug { "Guide Pulse started. guideOutput=$guideOutput, duration=$duration ms, direction=$direction" }
            delayTask.execute(job)
            LOG.debug { "Guide Pulse finished. guideOutput=$guideOutput, duration=$duration ms, direction=$direction" }
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

        @JvmStatic private val LOG = loggerFor<GuidePulseTask>()

        @JvmStatic
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
