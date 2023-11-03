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
import kotlin.time.Duration.Companion.milliseconds

data class GuidePulseTasklet(val request: GuidePulseRequest) : PublishSequenceTasklet<GuidePulseEvent>(), Consumer<DelayEvent> {

    private val delayTasklet = DelayTasklet(request.durationInMilliseconds.milliseconds)

    init {
        delayTasklet.subscribe(this)
    }

    override fun execute(contribution: StepContribution, chunkContext: ChunkContext): RepeatStatus {
        val guideOutput = requireNotNull(request.guideOutput)
        val durationInMilliseconds = request.durationInMilliseconds

        // Force stop in reversed direction.
        guideOutput.pulseGuide(0, request.direction.reversed)

        if (guideOutput.pulseGuide(durationInMilliseconds.toInt(), request.direction)) {
            delayTasklet.execute(contribution, chunkContext)
        }

        return RepeatStatus.FINISHED
    }

    override fun stop() {
        request.guideOutput?.pulseGuide(0, request.direction)
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
        private fun GuideOutput.pulseGuide(durationInMilliseconds: Int, direction: GuideDirection): Boolean {
            when (direction) {
                GuideDirection.NORTH -> guideNorth(durationInMilliseconds)
                GuideDirection.SOUTH -> guideSouth(durationInMilliseconds)
                GuideDirection.WEST -> guideWest(durationInMilliseconds)
                GuideDirection.EAST -> guideEast(durationInMilliseconds)
                else -> return false
            }

            return true
        }
    }
}
