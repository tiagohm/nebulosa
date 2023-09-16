package nebulosa.api.sequencer.tasklets.delay

import kotlin.time.Duration

data class DelayElapsed(
    val remainingTime: Duration,
    val delayTime: Duration,
    val waitTime: Duration,
)
