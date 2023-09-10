package nebulosa.api.tasklets.delay

import kotlin.time.Duration

fun interface DelayListener {

    fun onDelayElapsed(remainingTime: Duration, delayTime: Duration, waitTime: Duration)
}
