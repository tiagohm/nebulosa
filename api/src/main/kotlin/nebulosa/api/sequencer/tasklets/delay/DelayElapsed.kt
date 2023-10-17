package nebulosa.api.sequencer.tasklets.delay

import com.fasterxml.jackson.annotation.JsonIgnore
import nebulosa.api.sequencer.DelayEvent
import nebulosa.api.sequencer.SequenceStepEvent
import nebulosa.api.sequencer.SequenceTaskletEvent
import org.springframework.batch.core.StepExecution
import kotlin.time.Duration

data class DelayElapsed(
    override val remainingTime: Duration,
    override val delayTime: Duration,
    override val waitTime: Duration,
    @JsonIgnore override val stepExecution: StepExecution,
    @JsonIgnore override val tasklet: DelayTasklet,
) : SequenceStepEvent, SequenceTaskletEvent, DelayEvent {

    override val progress
        get() = if (remainingTime > Duration.ZERO) 1.0 - delayTime / remainingTime else 1.0

    inline val isStarted
        get() = remainingTime == delayTime

    inline val isFinished
        get() = remainingTime == Duration.ZERO
}
