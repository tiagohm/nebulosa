package nebulosa.api.guiding

import nebulosa.batch.processing.Step
import nebulosa.batch.processing.StepExecution
import nebulosa.batch.processing.StepResult
import nebulosa.batch.processing.delay.DelayStep
import nebulosa.batch.processing.delay.DelayStepListener
import nebulosa.guiding.GuideDirection
import nebulosa.indi.device.guide.GuideOutput
import java.time.Duration

data class GuidePulseStep(@JvmField val request: GuidePulseRequest) : Step, DelayStepListener {

    private val listeners = LinkedHashSet<GuidePulseListener>()
    private val delayStep = DelayStep(request.duration)

    init {
        delayStep.registerDelayStepListener(this)
    }

    fun registerGuidePulseListener(listener: GuidePulseListener) {
        listeners.add(listener)
    }

    fun unregisterGuidePulseListener(listener: GuidePulseListener) {
        listeners.remove(listener)
    }

    override fun execute(stepExecution: StepExecution): StepResult {
        val guideOutput = requireNotNull(request.guideOutput)

        if (guideOutput.pulseGuide(request.duration, request.direction)) {
            delayStep.execute(stepExecution)
        }

        return StepResult.FINISHED
    }

    override fun stop(mayInterruptIfRunning: Boolean) {
        request.guideOutput?.pulseGuide(Duration.ZERO, request.direction)
        delayStep.stop()
    }

    override fun onDelayElapsed(step: DelayStep, stepExecution: StepExecution) {
        listeners.forEach { it.onGuidePulseElapsed(this, stepExecution) }
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
