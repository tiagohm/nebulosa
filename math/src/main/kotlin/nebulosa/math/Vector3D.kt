@file:JvmName("Vector3D")

package nebulosa.math

import nebulosa.math.Angle.Companion.rad
import org.ejml.data.DMatrix3
import org.ejml.dense.fixed.CommonOps_DDF3
import kotlin.math.atan2
import kotlin.math.sqrt

@Suppress("NOTHING_TO_INLINE")
open class Vector3D(@PublishedApi internal val vector: DMatrix3) {

    constructor(a1: Double = 0.0, a2: Double = 0.0, a3: Double = 0.0) : this(DMatrix3(a1, a2, a3))

    constructor(a: DoubleArray, offset: Int = 0) : this(a[offset], a[offset + 1], a[offset + 2])

    inline operator fun get(index: Int) = vector[0, index]

    inline operator fun plus(other: Vector3D) = Vector3D().also { CommonOps_DDF3.add(vector, other.vector, it.vector) }

    inline operator fun minus(other: Vector3D) = Vector3D().also { CommonOps_DDF3.subtract(vector, other.vector, it.vector) }

    inline operator fun times(scalar: Double) = Vector3D().also { CommonOps_DDF3.scale(scalar, vector, it.vector) }

    inline operator fun div(scalar: Double) = Vector3D().also { CommonOps_DDF3.divide(vector, scalar, it.vector) }

    inline operator fun unaryMinus() = Vector3D(-a1, -a2, -a3)

    inline val a1 get() = vector.a1

    inline val a2 get() = vector.a2

    inline val a3 get() = vector.a3

    inline operator fun component1() = a1

    inline operator fun component2() = a2

    inline operator fun component3() = a3

    inline fun dot(other: Vector3D) = CommonOps_DDF3.dot(vector, other.vector)

    inline val length get() = sqrt(dot(this))

    inline val normalized get() = length.let { if (it == 0.0) this else this / it }

    fun isEmpty() = a1 == 0.0 && a2 == 0.0 && a3 == 0.0

    inline fun isNotEmpty() = !isEmpty()

    /**
     * Computes the angle between this vector and [vector].

     * @return The angle in radians.
     */
    fun angle(vector: Vector3D): Angle {
        val a = this * vector.length
        val b = vector * length
        return (2.0 * atan2((a - b).length, (a + b).length)).rad
    }

    override fun toString() = "Vector3D(a1=$a1, a2=$a2, a3=$a3)"

    companion object {

        @JvmStatic val EMPTY = Vector3D()

        @JvmStatic val X = Vector3D(a1 = 1.0)

        @JvmStatic val Y = Vector3D(a2 = 1.0)

        @JvmStatic val Z = Vector3D(a3 = 1.0)
    }
}
