package nebulosa.time

import nebulosa.erfa.eraSp00
import nebulosa.math.Matrix3D
import nebulosa.math.PairOfAngle
import nebulosa.math.TripleOfAngle
import nebulosa.math.arcsec

fun interface PolarMotion {

    fun pmXY(time: InstantOfTime): PairOfAngle

    /**
     * Computes the motion angles from the specified [time].
     */
    fun pmAngles(time: InstantOfTime): TripleOfAngle {
        val sprime = eraSp00(time.tt.whole, time.tt.fraction)
        val (x, y) = pmXY(time)
        return TripleOfAngle(sprime, x, y)
    }

    /**
     * Computes the motion matrix from the specified [time].
     */
    fun pmMatrix(time: InstantOfTime): Matrix3D {
        val (sprime, x, y) = pmAngles(time)
        return Matrix3D.rotX(y).rotateY(x).rotateZ(-sprime)
    }

    object None : PolarMotion {

        override fun pmXY(time: InstantOfTime) = PairOfAngle.ZERO

        override fun pmAngles(time: InstantOfTime) = TripleOfAngle.ZERO

        override fun pmMatrix(time: InstantOfTime) = Matrix3D.IDENTITY
    }
}
