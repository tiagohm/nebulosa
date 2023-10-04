package nebulosa.phd2.client.commands

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

data class Guide(
    val settlePixels: Double = 1.5,
    val settleTime: Duration = 10.seconds,
    val settleTimeout: Duration = 60.seconds,
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
            "pixels" to settlePixels, "time" to settleTime.inWholeSeconds,
            "timeout" to settleTimeout.inWholeSeconds,
        )
    )

    override val responseType = Int::class.java
}
