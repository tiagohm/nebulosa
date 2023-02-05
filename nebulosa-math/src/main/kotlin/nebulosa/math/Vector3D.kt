@file:JvmName("Vector3D")

package nebulosa.math

import nebulosa.math.Angle.Companion.rad
import kotlin.math.atan2
import kotlin.math.sqrt

@JvmInline
@Suppress("NOTHING_TO_INLINE")
value class Vector3D private constructor(@PublishedApi internal val vector: DoubleArray) {

    constructor(x: Double = 0.0, y: Double = 0.0, z: Double = 0.0) : this(doubleArrayOf(x, y, z))

    constructor(vector: DoubleArray, offset: Int = 0) : this(vector[offset], vector[offset + 1], vector[offset + 2])

    inline operator fun get(index: Int) = vector[index]

    inline operator fun plus(other: Vector3D) = Vector3D(x + other.x, y + other.y, z + other.z)

    inline operator fun minus(other: Vector3D) = Vector3D(x - other.x, y - other.y, z - other.z)

    inline operator fun times(scalar: Double) = Vector3D(x * scalar, y * scalar, z * scalar)

    inline operator fun div(scalar: Double) = Vector3D(x / scalar, y / scalar, z / scalar)

    inline operator fun unaryMinus() = Vector3D(-x, -y, -z)

    inline val x
        get() = vector[0]

    inline val y
        get() = vector[1]

    inline val z
        get() = vector[2]

    inline operator fun component1() = x

    inline operator fun component2() = y

    inline operator fun component3() = z

    inline fun dot(other: Vector3D) = x * other.x + y * other.y + z * other.z

    inline fun cross(other: Vector3D) = Vector3D(y * other.z - z * other.y, z * other.x - x * other.z, x * other.y - y * other.x)

    val length
        get() = sqrt(dot(this))

    val normalized
        get() = length.let { if (it == 0.0) this else this / it }

    fun isEmpty() = x == 0.0 && y == 0.0 && z == 0.0

    /**
     * Computes the angle between this vector and [vector].

     * @return The angle in radians.
     */
    fun angle(vector: Vector3D): Angle {
        val a = this * vector.length
        val b = vector * length
        return (2.0 * atan2((a - b).length, (a + b).length)).rad
    }

    override fun toString() = "Vector3D(x=$x, y=$y, z=$z)"

    companion object {

        @JvmStatic val EMPTY = Vector3D()
        @JvmStatic val X = Vector3D(x = 1.0)
        @JvmStatic val Y = Vector3D(y = 1.0)
        @JvmStatic val Z = Vector3D(z = 1.0)

        @JvmStatic
        fun of(other: Vector3D) = Vector3D(other.vector)
    }
}
