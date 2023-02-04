package nebulosa.time

import nebulosa.constants.J2000
import nebulosa.erfa.PairOfAngle
import nebulosa.erfa.TripleOfAngle
import nebulosa.math.Angle.Companion.arcsec
import nebulosa.math.Matrix3D

fun interface PolarMotion {

    fun pmXY(time: InstantOfTime): PairOfAngle

    /**
     * Computes the motion angles from the specified [time].
     */
    fun pmAngles(time: InstantOfTime): TripleOfAngle {
        val sprime = -47E-6 * (time.tdb.value - J2000) / 36525.0
        val (x, y) = pmXY(time)
        return TripleOfAngle(sprime.arcsec, x, y)
    }

    /**
     * Computes the motion matrix from the specified [time].
     */
    fun pmMatrix(time: InstantOfTime): Matrix3D {
        val (sprime, x, y) = pmAngles(time)
        return Matrix3D.IDENTITY.rotateX(y).rotateY(x).rotateZ(-sprime)
    }
}
