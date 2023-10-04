package nebulosa.phd2.client.commands

import kotlin.time.Duration

data class SetExposure(val duration: Duration) : PHD2Command<Int> {

    override val methodName = "set_exposure"

    override val params = listOf(duration.inWholeMilliseconds)

    override val responseType = Int::class.java
}
