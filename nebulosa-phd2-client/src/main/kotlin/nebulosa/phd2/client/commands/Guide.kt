package nebulosa.phd2.client.commands

import nebulosa.guiding.Guider
import kotlin.time.Duration

data class Guide(
    val settleAmount: Double = Guider.DEFAULT_SETTLE_AMOUNT,
    val settleTime: Duration = Guider.DEFAULT_SETTLE_TIME,
    val settleTimeout: Duration = Guider.DEFAULT_SETTLE_TIMEOUT,
    val recalibrate: Boolean = false,
    val x: Int = 0,
    val y: Int = 0,
    val width: Int = 0,
    val height: Int = 0,
) : PHD2Command<Int> {

    override val methodName = "guide"

    override val params = mapOf(
        "recalibrate" to recalibrate,
        "roi" to if (width > 0 && height > 0) listOf(x, y, width, height) else null,
        "settle" to mapOf(
            "pixels" to settleAmount, "time" to settleTime.inWholeSeconds,
            "timeout" to settleTimeout.inWholeSeconds,
        )
    )

    override val responseType = Int::class.java
}
