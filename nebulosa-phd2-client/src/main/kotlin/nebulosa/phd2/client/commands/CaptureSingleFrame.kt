package nebulosa.phd2.client.commands

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

data class CaptureSingleFrame(
    val exposure: Duration = 1.seconds,
    val x: Int = 0,
    val y: Int = 0,
    val width: Int = 0,
    val height: Int = 0,
) : PHD2Command<Int> {

    override val methodName = "capture_single_frame"

    override val params = mapOf(
        "exposure" to exposure.inWholeMilliseconds,
        "subframe" to if (width > 0 && height > 0) listOf(x, y, width, height) else null,
    )

    override val responseType = Int::class.java
}
