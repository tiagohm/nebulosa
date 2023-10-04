package nebulosa.phd2.client.commands

/**
 * Starts capturing, or, if guiding, stops guiding but continue capturing.
 */
data object Loop : PHD2Command<Int> {

    override val methodName = "loop"

    override val params = null

    override val responseType = Int::class.java
}
