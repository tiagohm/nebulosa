package nebulosa.phd2.client.commands

/**
 * Stop capturing (and stop guiding).
 */
data object StopCapture : PHD2Command<Int> {

    override val methodName = "stop_capture"

    override val params = null

    override val responseType = Int::class.java
}
