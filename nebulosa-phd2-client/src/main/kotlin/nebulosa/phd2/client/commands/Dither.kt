package nebulosa.phd2.client.commands

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

data class Dither(
    val amount: Double,
    val raOnly: Boolean = false,
    val settlePixels: Double = 1.5,
    val settleTime: Duration = 10.seconds,
    val settleTimeout: Duration = 60.seconds,
) : PHD2Command<Int> {

    override val methodName = "dither"

    override val params = mapOf(
        "amount" to amount, "raOnly" to raOnly,
        "settle" to mapOf(
            "pixels" to settlePixels, "time" to settleTime.inWholeSeconds,
            "timeout" to settleTimeout.inWholeSeconds,
        )
    )

    override val responseType = Int::class.java
}
