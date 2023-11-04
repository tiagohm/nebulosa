package nebulosa.api.sequencer.tasklets.delay

import com.fasterxml.jackson.annotation.JsonIgnore
import nebulosa.api.sequencer.SequenceStepEvent
import nebulosa.api.sequencer.SequenceTaskletEvent
import java.time.Duration

sealed interface DelayEvent : SequenceStepEvent, SequenceTaskletEvent {

    override val tasklet: DelayTasklet

    val remainingTime: Duration

    val waitDuration: Duration

    val isStarted
        @JsonIgnore get() = remainingTime == tasklet.duration

    val isFinished
        @JsonIgnore get() = remainingTime == Duration.ZERO
}
