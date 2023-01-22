package nebulosa.nova.frame

import nebulosa.erfa.*
import nebulosa.math.Matrix3D
import nebulosa.time.IERS
import nebulosa.time.InstantOfTime

/**
 * Polar motion matrix at the given [time].
 */
class TETEToITRSMatrix(
    val time: InstantOfTime,
    val rbpn: Matrix3D? = null,
) : Matrix3D(compute(time, rbpn)) {

    companion object {

        @JvmStatic
        private fun compute(
            time: InstantOfTime,
            rbpn: Matrix3D? = null,
        ): Matrix3D {
            val (xp, yp) = IERS.pmXY(time)
            val sp = eraSp00(time.tt.whole, time.tt.fraction)
            val pm = eraPom00(xp, yp, sp)
            // Now determine the greenwich apparent sidereal time.
            // We use the 2006A model for consistency with RBPN matrix use in GCRS <-> TETE.
            val gast = if (rbpn != null) {
                eraGst06(time.ut1.whole, time.ut1.fraction, time.tt.whole, time.tt.fraction, rbpn)
            } else {
                eraGst06a(time.ut1.whole, time.ut1.fraction, time.tt.whole, time.tt.fraction)
            }
            // c2tcio expects a GCRS->CIRS matrix, but we just set that to an I-matrix
            // because we're already in CIRS.
            return eraC2tcio(IDENTITY, gast, pm)
        }
    }
}
