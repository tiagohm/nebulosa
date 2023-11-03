package nebulosa.api.sequencer.tasklets.delay

import nebulosa.api.sequencer.SequenceStepEvent
import nebulosa.api.sequencer.SequenceTaskletEvent
import kotlin.time.Duration

sealed interface DelayEvent : SequenceStepEvent, SequenceTaskletEvent {

    override val tasklet: DelayTasklet

    val remainingTime: Duration

    val waitTime: Duration

    val isStarted
        get() = remainingTime == tasklet.duration

    val isFinished
        get() = remainingTime == Duration.ZERO
}
