package nebulosa.math

import kotlin.math.sqrt

interface Point3D : Point2D {

    val z: Double

    operator fun component3() = z

    override val length
        get() = sqrt(x * x + y * y + z * z)

    fun distance(other: Point3D): Double {
        val dx = x - other.x
        val dy = y - other.y
        val dz = z - other.z
        return sqrt(dx * dx + dy * dy + dz * dz)
    }

    override fun distance(other: Point2D): Double {
        val dx = x - other.x
        val dy = y - other.y
        return sqrt(dx * dx + dy * dy + z * z)
    }

    fun angle(other: Point3D): Angle {
        val dot = x * other.x + y * other.y + z * other.z
        return dot / (length * other.length)
    }

    override fun angle(other: Point2D): Angle {
        val dot = x * other.x + y * other.y
        return dot / (length * other.length)
    }

    data class XYZ(override val x: Double, override val y: Double, override val z: Double) : Point3D

    companion object {

        @JvmStatic val ZERO: Point3D = XYZ(0.0, 0.0, 0.0)

        @JvmStatic
        operator fun invoke(x: Double, y: Double, z: Double): Point3D = XYZ(x, y, z)
    }
}
