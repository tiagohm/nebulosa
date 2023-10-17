package nebulosa.api.sequencer

import kotlin.time.Duration

interface DelayEvent {

    val remainingTime: Duration

    val delayTime: Duration

    val waitTime: Duration

    val progress: Double
}
