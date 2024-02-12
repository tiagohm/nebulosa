package nebulosa.phd2.client.commands

import nebulosa.math.Point2D

/**
 * When [exact] is true, the lock position is moved to the exact given coordinates ([x], [y]).
 * When false, the current position is moved to the given coordinates and if
 * a guide star is in range, the lock position is set to the coordinates of the guide star.
 */
data class SetLockPosition(override val x: Double, override val y: Double, val exact: Boolean = true) : PHD2Command<Int>, Point2D {

    override val methodName = "set_lock_position"

    override val params = listOf(x, y, exact)

    override val responseType = Int::class.java
}
