package nebulosa.api.sequencer.tasklets.delay

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.batch.core.StepExecution
import java.time.Duration

data class DelayElapsed(
    override val remainingTime: Duration,
    override val waitDuration: Duration,
    @JsonIgnore override val stepExecution: StepExecution,
    @JsonIgnore override val tasklet: DelayTasklet,
) : DelayEvent {

    override val progress
        get() = if (remainingTime > Duration.ZERO) 1.0 - tasklet.duration.toNanos() / remainingTime.toNanos().toDouble()
        else 1.0
}
