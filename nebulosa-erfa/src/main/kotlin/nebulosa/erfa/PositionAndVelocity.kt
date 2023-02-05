package nebulosa.erfa

import nebulosa.math.Vector3D

@Suppress("NOTHING_TO_INLINE")
data class PositionAndVelocity(
    val position: Vector3D,
    val velocity: Vector3D,
) {

    inline operator fun unaryMinus() = PositionAndVelocity(-position, -velocity)

    companion object {

        @JvmStatic val EMPTY = PositionAndVelocity(Vector3D.EMPTY, Vector3D.EMPTY)
    }
}
