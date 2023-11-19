package nebulosa.phd2.client.commands

import java.time.Duration

data class CaptureSingleFrame(
    val exposure: Duration,
    val x: Int = 0,
    val y: Int = 0,
    val width: Int = 0,
    val height: Int = 0,
) : PHD2Command<Int> {

    override val methodName = "capture_single_frame"

    override val params = mapOf(
        "exposure" to exposure.toMillis(),
        "subframe" to if (width > 0 && height > 0) listOf(x, y, width, height) else null,
    )

    override val responseType = Int::class.java
}
