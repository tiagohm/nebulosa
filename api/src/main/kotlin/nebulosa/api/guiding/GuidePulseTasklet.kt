package nebulosa.api.guiding

import io.reactivex.rxjava3.functions.Consumer
import nebulosa.api.sequencer.SubjectSequenceTasklet
import nebulosa.api.sequencer.tasklets.delay.DelayElapsed
import nebulosa.api.sequencer.tasklets.delay.DelayTasklet
import nebulosa.guiding.GuideDirection
import nebulosa.indi.device.guide.GuideOutput
import org.springframework.batch.core.StepContribution
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.repeat.RepeatStatus
import kotlin.time.Duration

data class GuidePulseTasklet(
    val guideOutput: GuideOutput,
    val direction: GuideDirection, val duration: Duration,
) : SubjectSequenceTasklet<GuidePulseEvent>(), Consumer<DelayElapsed> {

    private val delayTasklet = DelayTasklet(duration)

    init {
        delayTasklet.subscribe(this)
    }

    override fun execute(contribution: StepContribution, chunkContext: ChunkContext): RepeatStatus {
        val durationInMilliseconds = duration.inWholeMilliseconds

        // Force stop in reversed direction.
        pulseGuide(0, direction.reversed)

        if (pulseGuide(durationInMilliseconds.toInt())) {
            delayTasklet.execute(contribution, chunkContext)
        }

        return RepeatStatus.FINISHED
    }

    override fun stop() {
        pulseGuide(0)
        delayTasklet.stop()
    }

    override fun accept(event: DelayElapsed) {
        if (event.isStarted) onNext(GuidePulseStarted(event.stepExecution, this))
        else if (event.isFinished) onNext(GuidePulseFinished(event.stepExecution, this))
        else {
            val remainingTime = event.remainingTime.inWholeMicroseconds
            onNext(GuidePulseElapsed(remainingTime, event.progress, direction, event.stepExecution, this))
        }
    }

    private fun pulseGuide(durationInMilliseconds: Int, direction: GuideDirection = this.direction): Boolean {
        when (direction) {
            GuideDirection.NORTH -> guideOutput.guideNorth(durationInMilliseconds)
            GuideDirection.SOUTH -> guideOutput.guideSouth(durationInMilliseconds)
            GuideDirection.WEST -> guideOutput.guideWest(durationInMilliseconds)
            GuideDirection.EAST -> guideOutput.guideEast(durationInMilliseconds)
            else -> return false
        }

        return true
    }
}
