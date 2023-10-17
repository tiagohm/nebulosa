package nebulosa.phd2.client.commands

/**
 * Connects or disconnects all equipment.
 */
data class SetConnected(val connected: Boolean) : PHD2Command<Int> {

    override val methodName = "set_connected"

    override val params = listOf(connected)

    override val responseType = Int::class.java
}
