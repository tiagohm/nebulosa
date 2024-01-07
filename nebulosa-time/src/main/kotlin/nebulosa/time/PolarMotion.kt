package nebulosa.time

import nebulosa.erfa.eraSp00
import nebulosa.math.Matrix3D

fun interface PolarMotion {

    fun pmXY(time: InstantOfTime): DoubleArray

    /**
     * Computes the motion angles from the specified [time].
     */
    fun pmAngles(time: InstantOfTime): DoubleArray {
        val sprime = eraSp00(time.tt.whole, time.tt.fraction)
        val (x, y) = pmXY(time)
        return doubleArrayOf(sprime, x, y)
    }

    /**
     * Computes the motion matrix from the specified [time].
     */
    fun pmMatrix(time: InstantOfTime): Matrix3D {
        val (sprime, x, y) = pmAngles(time)
        return Matrix3D.rotX(y).rotateY(x).rotateZ(-sprime)
    }

    object None : PolarMotion {

        override fun pmXY(time: InstantOfTime) = doubleArrayOf(0.0, 0.0)

        override fun pmAngles(time: InstantOfTime) = doubleArrayOf(0.0, 0.0, 0.0)

        override fun pmMatrix(time: InstantOfTime) = Matrix3D.IDENTITY
    }
}
