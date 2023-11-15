package nebulosa.phd2.client.commands

import java.time.Duration

data class SetExposure(val duration: Duration) : PHD2Command<Int> {

    override val methodName = "set_exposure"

    override val params = listOf(duration.toMillis())

    override val responseType = Int::class.java
}
