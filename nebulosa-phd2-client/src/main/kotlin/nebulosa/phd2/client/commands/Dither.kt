package nebulosa.phd2.client.commands

import nebulosa.guiding.Guider
import java.time.Duration

data class Dither(
    val amount: Double,
    val raOnly: Boolean = false,
    val settleAmount: Double = Guider.DEFAULT_SETTLE_AMOUNT,
    val settleTime: Duration = Guider.DEFAULT_SETTLE_TIME,
    val settleTimeout: Duration = Guider.DEFAULT_SETTLE_TIMEOUT,
) : PHD2Command<Int> {

    override val methodName = "dither"

    override val params = mapOf(
        "amount" to amount, "raOnly" to raOnly,
        "settle" to mapOf(
            "pixels" to settleAmount, "time" to settleTime.toSeconds(),
            "timeout" to settleTimeout.toSeconds(),
        )
    )

    override val responseType = Int::class.java
}
