package nebulosa.api.guiding

import io.reactivex.rxjava3.functions.Consumer
import nebulosa.api.sequencer.PublishSequenceTasklet
import nebulosa.api.sequencer.tasklets.delay.DelayEvent
import nebulosa.api.sequencer.tasklets.delay.DelayTasklet
import nebulosa.guiding.GuideDirection
import nebulosa.indi.device.guide.GuideOutput
import org.springframework.batch.core.StepContribution
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.repeat.RepeatStatus
import java.time.Duration

data class GuidePulseTasklet(val request: GuidePulseRequest) : PublishSequenceTasklet<GuidePulseEvent>(), Consumer<DelayEvent> {

    private val delayTasklet = DelayTasklet(request.duration)

    init {
        delayTasklet.subscribe(this)
    }

    override fun execute(contribution: StepContribution, chunkContext: ChunkContext): RepeatStatus {
        val guideOutput = requireNotNull(request.guideOutput)
        val durationInMilliseconds = request.duration

        // Force stop in reversed direction.
        guideOutput.pulseGuide(Duration.ZERO, request.direction.reversed)

        if (guideOutput.pulseGuide(durationInMilliseconds, request.direction)) {
            delayTasklet.execute(contribution, chunkContext)
        }

        return RepeatStatus.FINISHED
    }

    override fun stop() {
        request.guideOutput?.pulseGuide(Duration.ZERO, request.direction)
        delayTasklet.stop()
    }

    override fun accept(event: DelayEvent) {
        val guidePulseEvent = if (event.isStarted) GuidePulseStarted(event.stepExecution, this)
        else if (event.isFinished) GuidePulseFinished(event.stepExecution, this)
        else GuidePulseElapsed(event.remainingTime, event.progress, request.direction, event.stepExecution, this)

        onNext(guidePulseEvent)
    }

    companion object {

        @JvmStatic
        private fun GuideOutput.pulseGuide(duration: Duration, direction: GuideDirection): Boolean {
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
