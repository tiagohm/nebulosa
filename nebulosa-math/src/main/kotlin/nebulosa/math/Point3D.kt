package nebulosa.math

import kotlin.math.sqrt

interface Point3D : Point2D {

    val z: Double

    operator fun component3() = z

    fun distance(other: Point3D): Double {
        val dx = x - other.x
        val dy = y - other.y
        val dz = z - other.z
        return sqrt(dx * dx + dy * dy + dz * dz)
    }
}
