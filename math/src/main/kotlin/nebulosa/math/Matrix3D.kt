@file:JvmName("Matrix3D")

package nebulosa.math

import org.ejml.data.DMatrix3x3
import org.ejml.dense.fixed.CommonOps_DDF3

@Suppress("NOTHING_TO_INLINE")
open class Matrix3D(@PublishedApi internal val matrix: DMatrix3x3) {

    constructor(matrix: Matrix3D) : this(matrix.matrix)

    constructor(
        a11: Double = 0.0, a12: Double = 0.0, a13: Double = 0.0,
        a21: Double = 0.0, a22: Double = 0.0, a23: Double = 0.0,
        a31: Double = 0.0, a32: Double = 0.0, a33: Double = 0.0,
    ) : this(DMatrix3x3(a11, a12, a13, a21, a22, a23, a31, a32, a33))

    inline val a11 get() = matrix.a11

    inline val a12 get() = matrix.a12

    inline val a13 get() = matrix.a13

    inline val a21 get() = matrix.a21

    inline val a22 get() = matrix.a22

    inline val a23 get() = matrix.a23

    inline val a31 get() = matrix.a31

    inline val a32 get() = matrix.a32

    inline val a33 get() = matrix.a33

    inline val determinant get() = CommonOps_DDF3.det(matrix)

    inline val trace get() = CommonOps_DDF3.trace(matrix)

    inline operator fun get(row: Int, column: Int) = matrix[row, column]

    protected inline operator fun set(row: Int, column: Int, value: Double) {
        matrix[row, column] = value
    }

    inline operator fun component1() = a11

    inline operator fun component2() = a12

    inline operator fun component3() = a13

    inline operator fun component4() = a21

    inline operator fun component5() = a22

    inline operator fun component6() = a23

    inline operator fun component7() = a31

    inline operator fun component8() = a32

    inline operator fun component9() = a33

    inline operator fun plus(other: Matrix3D) = Matrix3D().also { CommonOps_DDF3.add(matrix, other.matrix, it.matrix) }

    inline operator fun minus(other: Matrix3D) = Matrix3D().also { CommonOps_DDF3.subtract(matrix, other.matrix, it.matrix) }

    inline operator fun times(other: Matrix3D) = Matrix3D().also { CommonOps_DDF3.mult(matrix, other.matrix, it.matrix) }

    inline operator fun times(other: Vector3D) = Vector3D().also { CommonOps_DDF3.mult(matrix, other.vector, it.vector) }

    inline operator fun times(scalar: Double) = Matrix3D().also { CommonOps_DDF3.scale(scalar, matrix, it.matrix) }

    inline operator fun div(scalar: Double) = Matrix3D().also { CommonOps_DDF3.divide(matrix, scalar, it.matrix) }

    val transposed by lazy { Matrix3D().also { CommonOps_DDF3.transpose(matrix, it.matrix) } }

    inline operator fun unaryMinus() = Matrix3D(-a11, -a12, -a13, -a21, -a22, -a23, -a31, -a32, -a33)

    infix fun rotateX(angle: Angle): Matrix3D {
        val ca = angle.cos
        val sa = angle.sin
        return Matrix3D(1.0, 0.0, 0.0, 0.0, ca, sa, 0.0, -sa, ca) * this
    }

    infix fun rotateY(angle: Angle): Matrix3D {
        val ca = angle.cos
        val sa = angle.sin
        return Matrix3D(ca, 0.0, -sa, 0.0, 1.0, 0.0, sa, 0.0, ca) * this
    }

    infix fun rotateZ(angle: Angle): Matrix3D {
        val ca = angle.cos
        val sa = angle.sin
        return Matrix3D(ca, sa, 0.0, -sa, ca, 0.0, 0.0, 0.0, 1.0) * this
    }

    inline fun flipX() = Matrix3D(a31, a32, a33, a21, a22, a23, a11, a12, a13)

    inline fun flipY() = Matrix3D(a13, a12, a11, a23, a22, a21, a33, a32, a31)

    fun isEmpty() = a11 == 0.0 && a12 == 0.0 && a13 == 0.0 &&
            a21 == 0.0 && a22 == 0.0 && a23 == 0.0 &&
            a31 == 0.0 && a32 == 0.0 && a33 == 0.0

    inline fun isNotEmpty() = !isEmpty()

    override fun toString(): String {
        return "Matrix3D(a11=$a11, a12=$a12, a13=$a13, a21=$a21, a22=$a22, a23=$a23, a31=$a31, a32=$a32, a33=$a33)"
    }

    companion object {

        @JvmStatic val IDENTITY = Matrix3D(a11 = 1.0, a22 = 1.0, a33 = 1.0)
    }
}
