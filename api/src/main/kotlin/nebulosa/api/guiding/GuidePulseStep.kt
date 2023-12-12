package nebulosa.api.guiding

import nebulosa.batch.processing.Step
import nebulosa.batch.processing.StepExecution
import nebulosa.batch.processing.StepResult
import nebulosa.batch.processing.delay.DelayListener
import nebulosa.batch.processing.delay.DelayStep
import nebulosa.guiding.GuideDirection
import nebulosa.indi.device.guide.GuideOutput
import java.time.Duration

data class GuidePulseStep(val request: GuidePulseRequest) : Step, DelayListener {

    private val listeners = HashSet<GuidePulseListener>()
    private val delayStep = DelayStep(request.duration)

    init {
        delayStep.registerListener(this)
    }

    fun registerListener(listener: GuidePulseListener) {
        listeners.add(listener)
    }

    fun unregisterListener(listener: GuidePulseListener) {
        listeners.remove(listener)
    }

    override fun execute(stepExecution: StepExecution): StepResult {
        val guideOutput = requireNotNull(request.guideOutput)

        // Force stop in reversed direction.
        guideOutput.pulseGuide(Duration.ZERO, request.direction.reversed)

        if (guideOutput.pulseGuide(request.duration, request.direction)) {
            delayStep.execute(stepExecution)
        }

        return StepResult.FINISHED
    }

    override fun stop(mayInterruptIfRunning: Boolean) {
        request.guideOutput?.pulseGuide(Duration.ZERO, request.direction)
        delayStep.stop()
    }

    @Suppress("NAME_SHADOWING")
    override fun onDelayElapsed(stepExecution: StepExecution) {
        val stepExecution = stepExecution.copy(step = this)
        listeners.forEach { it.onGuidePulseElapsed(stepExecution) }
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
