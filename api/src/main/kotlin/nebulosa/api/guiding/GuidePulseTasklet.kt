package nebulosa.api.guiding

import nebulosa.guiding.GuideDirection
import nebulosa.indi.device.guide.GuideOutput
import org.springframework.batch.core.StepContribution
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.core.step.tasklet.StoppableTasklet
import org.springframework.batch.repeat.RepeatStatus
import kotlin.time.Duration

data class GuidePulseTasklet(
    private val guideOutput: GuideOutput,
    private val direction: GuideDirection, private val duration: Duration,
    private val listener: GuidePulseListener,
) : StoppableTasklet {

    override fun execute(contribution: StepContribution, chunkContext: ChunkContext): RepeatStatus {
        val durationInMilliseconds = duration.inWholeMilliseconds.toInt()

        if (guideTo(durationInMilliseconds)) {
            listener.onGuidePulseStarted(guideOutput, direction, duration)
            Thread.sleep(durationInMilliseconds.toLong())
            listener.onGuidePulseFinished(guideOutput, direction, duration)
        }

        return RepeatStatus.FINISHED
    }

    override fun stop() {
        guideTo(0)
    }

    private fun guideTo(durationInMilliseconds: Int): Boolean {
        when (direction) {
            GuideDirection.UP_NORTH -> guideOutput.guideNorth(durationInMilliseconds)
            GuideDirection.DOWN_SOUTH -> guideOutput.guideSouth(durationInMilliseconds)
            GuideDirection.LEFT_WEST -> guideOutput.guideWest(durationInMilliseconds)
            GuideDirection.RIGHT_EAST -> guideOutput.guideEast(durationInMilliseconds)
            else -> return false
        }

        return true
    }
}
