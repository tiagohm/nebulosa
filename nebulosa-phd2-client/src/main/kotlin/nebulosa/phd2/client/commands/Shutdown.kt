package nebulosa.phd2.client.commands

/**
 * Closes PHD2.
 */
data object Shutdown : PHD2Command<Int> {

    override val methodName = "shutdown"

    override val params = null

    override val responseType = Int::class.java
}
