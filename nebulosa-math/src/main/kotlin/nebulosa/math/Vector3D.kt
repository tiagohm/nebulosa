package nebulosa.math

import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.sqrt

@Suppress("NOTHING_TO_INLINE")
open class Vector3D protected constructor(@PublishedApi @JvmField internal val vector: DoubleArray) : Point3D, Cloneable {

    constructor(x: Double = 0.0, y: Double = 0.0, z: Double = 0.0) : this(doubleArrayOf(x, y, z))

    constructor(vector: DoubleArray, offset: Int = 0) : this(vector[offset], vector[offset + 1], vector[offset + 2])

    inline operator fun get(index: Int) = vector[index]

    inline operator fun plus(other: Vector3D) = Vector3D(vector[0] + other[0], vector[1] + other[1], vector[2] + other[2])

    inline operator fun minus(other: Vector3D) = Vector3D(vector[0] - other[0], vector[1] - other[1], vector[2] - other[2])

    inline operator fun times(scalar: Double) = Vector3D(vector[0] * scalar, vector[1] * scalar, vector[2] * scalar)

    inline operator fun times(vector: Vector3D) = Vector3D(vector[0] * vector[0], vector[1] * vector[1], vector[2] * vector[2])

    inline operator fun div(scalar: Double) = Vector3D(vector[0] / scalar, vector[1] / scalar, vector[2] / scalar)

    inline operator fun unaryMinus() = Vector3D(-vector[0], -vector[1], -vector[2])

    override val x
        get() = vector[0]

    override val y
        get() = vector[1]

    override val z
        get() = vector[2]

    inline fun array() = vector.copyOf()

    /**
     * Scalar product between this vector and [other].
     */
    inline fun dot(other: Vector3D) = dot(other.vector)

    /**
     * Scalar product between this vector and [other].
     */
    inline fun dot(other: DoubleArray) = vector[0] * other[0] + vector[1] * other[1] + vector[2] * other[2]

    /**
     * Cross product between this vector and [other].
     */
    inline fun cross(other: Vector3D) = Vector3D(
        vector[1] * other[2] - vector[2] * other[1],
        vector[2] * other[0] - vector[0] * other[2],
        vector[0] * other[1] - vector[1] * other[0]
    )

    inline val length
        get() = sqrt(dot(this))

    inline val normalized
        get() = length.let { if (it == 0.0) this else this / it }

    inline val latitude
        get() = acos(vector[2]).rad

    inline val longitude
        get() = atan2(vector[1], vector[0]).rad.normalized

    inline fun isEmpty() = vector[0] == 0.0 && vector[1] == 0.0 && vector[2] == 0.0

    /**
     * Computes the angle between this vector and [vector].
     */
    fun angle(coordinate: Vector3D): Angle {
        // val a = this * vector.length
        // val b = vector * length
        // return (2.0 * atan2((a - b).length, (a + b).length)).rad

        val dot = dot(coordinate)
        val v = dot / (length * coordinate.length)
        return if (abs(v) > 1.0) if (v < 0.0) SEMICIRCLE else 0.0
        else acos(v).rad
    }

    override fun clone() = Vector3D(vector.copyOf())

    /**
     * Rotates this vector given an [axis] and [angle] of rotation.
     */
    fun rotate(axis: Vector3D, angle: Angle): Vector3D {
        return rotateByRodrigues(this, axis, angle)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Vector3D) return false

        if (!vector.contentEquals(other.vector)) return false

        return true
    }

    override fun hashCode() = vector.contentHashCode()

    override fun toString() = "${javaClass.simpleName}(x=$x, y=$y, z=$z)"

    companion object {

        @JvmStatic val EMPTY = Vector3D()
        @JvmStatic val X = Vector3D(x = 1.0)
        @JvmStatic val Y = Vector3D(y = 1.0)
        @JvmStatic val Z = Vector3D(z = 1.0)

        /**
         * Efficient algorithm for rotating a vector in space, given an [axis] and [angle] of rotation.
         *
         * @param v A vector in R³.
         * @param axis A vector describing an axis of rotation about which [v] rotates.
         * @param angle The angle that [v] should rotate by.
         *
         * @see <a href="https://en.wikipedia.org/wiki/Rodrigues'_rotation_formula">Wiki</a>
         */
        @JvmStatic
        fun rotateByRodrigues(v: Vector3D, axis: Vector3D, angle: Angle): Vector3D {
            val cosa = angle.cos
            val k = axis.normalized
            return v * cosa + k.cross(v) * angle.sin + k * k.dot(v) * (1.0 - cosa)
        }

        /**
         * Determines the plane that goes through the three points [a], [b] and [c]
         * and its defining vector.
         */
        @JvmStatic
        fun plane(a: Vector3D, b: Vector3D, c: Vector3D): Vector3D {
            return (b - a).cross(c - b)
        }
    }
}
