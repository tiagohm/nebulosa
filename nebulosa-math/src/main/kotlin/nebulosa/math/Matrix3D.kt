package nebulosa.math

@Suppress("NOTHING_TO_INLINE")
open class Matrix3D(@PublishedApi @JvmField internal val matrix: DoubleArray) : Cloneable {

    constructor(
        a11: Double = 0.0, a12: Double = 0.0, a13: Double = 0.0,
        a21: Double = 0.0, a22: Double = 0.0, a23: Double = 0.0,
        a31: Double = 0.0, a32: Double = 0.0, a33: Double = 0.0,
    ) : this(doubleArrayOf(a11, a12, a13, a21, a22, a23, a31, a32, a33))

    inline val a11
        get() = matrix[0]

    inline val a12
        get() = matrix[1]

    inline val a13
        get() = matrix[2]

    inline val a21
        get() = matrix[3]

    inline val a22
        get() = matrix[4]

    inline val a23
        get() = matrix[5]

    inline val a31
        get() = matrix[6]

    inline val a32
        get() = matrix[7]

    inline val a33
        get() = matrix[8]

    // TODO: Potentially less stable than using LU decomposition
    val determinant: Double
        get() {
            val a = a11 * (a22 * a33 - a23 * a32)
            val b = a12 * (a21 * a33 - a23 * a31)
            val c = a13 * (a21 * a32 - a31 * a22)
            return a - b + c
        }

    inline val trace
        get() = a11 + a22 + a33

    inline operator fun get(index: Int) = matrix[index]

    inline operator fun get(row: Int, column: Int) = matrix[row * 3 + column]

    inline operator fun component1() = a11

    inline operator fun component2() = a12

    inline operator fun component3() = a13

    inline operator fun component4() = a21

    inline operator fun component5() = a22

    inline operator fun component6() = a23

    inline operator fun component7() = a31

    inline operator fun component8() = a32

    inline operator fun component9() = a33

    inline operator fun plus(other: Matrix3D) = Matrix3D(
        a11 + other.a11, a12 + other.a12, a13 + other.a13,
        a21 + other.a21, a22 + other.a22, a23 + other.a23,
        a31 + other.a31, a32 + other.a32, a33 + other.a33,
    )

    inline operator fun plus(scalar: Double) = Matrix3D(
        a11 + scalar, a12 + scalar, a13 + scalar,
        a21 + scalar, a22 + scalar, a23 + scalar,
        a31 + scalar, a32 + scalar, a33 + scalar,
    )

    inline operator fun minus(other: Matrix3D) = Matrix3D(
        a11 - other.a11, a12 - other.a12, a13 - other.a13,
        a21 - other.a21, a22 - other.a22, a23 - other.a23,
        a31 - other.a31, a32 - other.a32, a33 - other.a33,
    )

    inline operator fun minus(scalar: Double) = Matrix3D(
        a11 - scalar, a12 - scalar, a13 - scalar,
        a21 - scalar, a22 - scalar, a23 - scalar,
        a31 - scalar, a32 - scalar, a33 - scalar,
    )

    inline operator fun times(other: Matrix3D) = Matrix3D(
        a11 * other.a11 + a12 * other.a21 + a13 * other.a31,
        a11 * other.a12 + a12 * other.a22 + a13 * other.a32,
        a11 * other.a13 + a12 * other.a23 + a13 * other.a33,
        a21 * other.a11 + a22 * other.a21 + a23 * other.a31,
        a21 * other.a12 + a22 * other.a22 + a23 * other.a32,
        a21 * other.a13 + a22 * other.a23 + a23 * other.a33,
        a31 * other.a11 + a32 * other.a21 + a33 * other.a31,
        a31 * other.a12 + a32 * other.a22 + a33 * other.a32,
        a31 * other.a13 + a32 * other.a23 + a33 * other.a33,
    )

    inline operator fun times(other: Vector3D) = Vector3D(
        a11 * other.vector[0] + a12 * other.vector[1] + a13 * other.vector[2],
        a21 * other.vector[0] + a22 * other.vector[1] + a23 * other.vector[2],
        a31 * other.vector[0] + a32 * other.vector[1] + a33 * other.vector[2],
    )

    inline operator fun times(scalar: Double) = Matrix3D(
        a11 * scalar, a12 * scalar, a13 * scalar,
        a21 * scalar, a22 * scalar, a23 * scalar,
        a31 * scalar, a32 * scalar, a33 * scalar,
    )

    inline operator fun div(scalar: Double) = Matrix3D(
        a11 / scalar, a12 / scalar, a13 / scalar,
        a21 / scalar, a22 / scalar, a23 / scalar,
        a31 / scalar, a32 / scalar, a33 / scalar,
    )

    inline operator fun unaryMinus() = Matrix3D(-a11, -a12, -a13, -a21, -a22, -a23, -a31, -a32, -a33)

    inline val transposed
        get() = Matrix3D(a11, a21, a31, a12, a22, a32, a13, a23, a33)

    infix fun rotateX(angle: Angle): Matrix3D {
        val ca = angle.cos
        val sa = angle.sin

        return Matrix3D(
            matrix[0], matrix[1], matrix[2],
            ca * matrix[3] + sa * matrix[6], ca * matrix[4] + sa * matrix[7], ca * matrix[5] + sa * matrix[8],
            -sa * matrix[3] + ca * matrix[6], -sa * matrix[4] + ca * matrix[7], -sa * matrix[5] + ca * matrix[8],
        )
    }

    infix fun rotateY(angle: Angle): Matrix3D {
        val ca = angle.cos
        val sa = angle.sin

        return Matrix3D(
            ca * matrix[0] - sa * matrix[6], ca * matrix[1] - sa * matrix[7], ca * matrix[2] - sa * matrix[8],
            matrix[3], matrix[4], matrix[5],
            sa * matrix[0] + ca * matrix[6], sa * matrix[1] + ca * matrix[7], sa * matrix[2] + ca * matrix[8],
        )
    }

    infix fun rotateZ(angle: Angle): Matrix3D {
        val ca = angle.cos
        val sa = angle.sin

        return Matrix3D(
            ca * matrix[0] + sa * matrix[3], ca * matrix[1] + sa * matrix[4], ca * matrix[2] + sa * matrix[5],
            -sa * matrix[0] + ca * matrix[3], -sa * matrix[1] + ca * matrix[4], -sa * matrix[2] + ca * matrix[5],
            matrix[6], matrix[7], matrix[8],
        )
    }

    inline fun flipX() = Matrix3D(a31, a32, a33, a21, a22, a23, a11, a12, a13)

    inline fun flipY() = Matrix3D(a13, a12, a11, a23, a22, a21, a33, a32, a31)

    override fun clone() = Matrix3D(matrix.copyOf())

    fun isEmpty() = a11 == 0.0 && a12 == 0.0 && a13 == 0.0 &&
            a21 == 0.0 && a22 == 0.0 && a23 == 0.0 &&
            a31 == 0.0 && a32 == 0.0 && a33 == 0.0

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Matrix3D) return false

        if (!matrix.contentEquals(other.matrix)) return false

        return true
    }

    override fun hashCode() = matrix.contentHashCode()

    override fun toString() = "Matrix3D(a11=$a11, a12=$a12, a13=$a13, a21=$a21, a22=$a22, a23=$a23, a31=$a31, a32=$a32, a33=$a33)"

    companion object {

        @JvmStatic val EMPTY = Matrix3D()
        @JvmStatic val IDENTITY = Matrix3D(a11 = 1.0, a22 = 1.0, a33 = 1.0)

        @JvmStatic
        fun of(other: Matrix3D) = Matrix3D(other.matrix)

        @JvmStatic
        fun rotX(angle: Angle): Matrix3D {
            val ca = angle.cos
            val sa = angle.sin
            return Matrix3D(1.0, 0.0, 0.0, 0.0, ca, sa, 0.0, -sa, ca)
        }

        @JvmStatic
        fun rotY(angle: Angle): Matrix3D {
            val ca = angle.cos
            val sa = angle.sin
            return Matrix3D(ca, 0.0, -sa, 0.0, 1.0, 0.0, sa, 0.0, ca)
        }

        @JvmStatic
        fun rotZ(angle: Angle): Matrix3D {
            val ca = angle.cos
            val sa = angle.sin
            return Matrix3D(ca, sa, 0.0, -sa, ca, 0.0, 0.0, 0.0, 1.0)
        }
    }
}
