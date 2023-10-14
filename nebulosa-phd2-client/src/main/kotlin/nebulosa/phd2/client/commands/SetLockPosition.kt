package nebulosa.phd2.client.commands

/**
 * When [exact] is true, the lock position is moved to the exact given coordinates ([x], [y]).
 * When false, the current position is moved to the given coordinates and if
 * a guide star is in range, the lock position is set to the coordinates of the guide star.
 */
data class SetLockPosition(val x: Double, val y: Double, val exact: Boolean = true) : PHD2Command<Int> {

    override val methodName = "set_lock_position"

    override val params = listOf(x, y, exact)

    override val responseType = Int::class.java
}
