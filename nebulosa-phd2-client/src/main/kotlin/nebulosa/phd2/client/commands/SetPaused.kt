package nebulosa.phd2.client.commands

/**
 * When setting paused to true, an optional second parameter with value "FULL" can be provided
 * to fully pause PHD2, including pausing looping exposures.
 * Otherwise, exposures continue to loop, and only guide output is paused.
 */
data class SetPaused(val paused: Boolean, val full: Boolean = false) : PHD2Command<Int> {

    override val methodName = "set_paused"

    override val params = listOf(paused, if (full) "full" else null)

    override val responseType = Int::class.java
}
