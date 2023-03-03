package nebulosa.time

import nebulosa.constants.J2000
import nebulosa.math.Angle.Companion.arcsec
import nebulosa.math.Matrix3D
import nebulosa.math.PairOfAngle
import nebulosa.math.TripleOfAngle

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
        return Matrix3D.rotateX(y).rotateY(x).rotateZ(-sprime)
    }
}
