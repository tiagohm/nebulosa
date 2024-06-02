package nebulosa.api.guiding

import io.reactivex.rxjava3.functions.Consumer
import nebulosa.api.tasks.AbstractTask
import nebulosa.api.tasks.delay.DelayEvent
import nebulosa.api.tasks.delay.DelayTask
import nebulosa.common.concurrency.cancel.CancellationListener
import nebulosa.common.concurrency.cancel.CancellationSource
import nebulosa.common.concurrency.cancel.CancellationToken
import nebulosa.guiding.GuideDirection
import nebulosa.indi.device.guide.GuideOutput
import nebulosa.log.loggerFor
import java.time.Duration

data class GuidePulseTask(
    @JvmField val guideOutput: GuideOutput,
    @JvmField val request: GuidePulseRequest,
) : AbstractTask<GuidePulseEvent>(), CancellationListener, Consumer<DelayEvent> {

    private val delayTask = DelayTask(request.duration)

    init {
        delayTask.subscribe(this)
    }

    override fun execute(cancellationToken: CancellationToken) {
        if (!cancellationToken.isCancelled && guideOutput.pulseGuide(request.duration, request.direction)) {
            LOG.info("Guide Pulse started. guideOutput={}, duration={}, direction={}", guideOutput, request.duration.toMillis(), request.direction)

            try {
                cancellationToken.listen(this)
                delayTask.execute(cancellationToken)
            } finally {
                cancellationToken.unlisten(this)
            }

            LOG.info("Guide Pulse finished. guideOutput={}, duration={}, direction={}", guideOutput, request.duration.toMillis(), request.direction)
        }
    }

    override fun onCancel(source: CancellationSource) {
        guideOutput.pulseGuide(Duration.ZERO, request.direction)
    }

    override fun accept(event: DelayEvent) {
        onNext(GuidePulseEvent(this, event.remainingTime, event.progress))
    }

    override fun close() {
        delayTask.close()
        super.close()
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
