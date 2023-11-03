package nebulosa.api.guiding

import com.fasterxml.jackson.annotation.JsonIgnore
import nebulosa.guiding.GuideDirection
import org.springframework.batch.core.StepExecution
import kotlin.time.Duration

data class GuidePulseElapsed(
    val remainingTime: Duration,
    override val progress: Double,
    val direction: GuideDirection,
    @JsonIgnore override val stepExecution: StepExecution,
    @JsonIgnore override val tasklet: GuidePulseTasklet,
) : GuidePulseEvent
