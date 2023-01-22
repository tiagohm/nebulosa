package nebulosa.nova.frame

import nebulosa.erfa.eraC2tcio
import nebulosa.erfa.eraEra00
import nebulosa.erfa.eraPom00
import nebulosa.erfa.eraSp00
import nebulosa.math.Matrix3D
import nebulosa.time.IERS
import nebulosa.time.InstantOfTime

class CIRSToITRSMatrix(val time: InstantOfTime) : Matrix3D(compute(time)) {

    companion object {

        @JvmStatic
        private fun compute(time: InstantOfTime): Matrix3D {
            // Compute the polar motion p-matrix.
            val (xp, yp) = IERS.pmXY(time)
            val sp = eraSp00(time.tt.whole, time.tt.fraction)
            val pm = eraPom00(xp, yp, sp)
            // Now determine the Earth Rotation Angle.
            val era = eraEra00(time.ut1.whole, time.ut1.fraction)
            // c2tcio expects a GCRS->CIRS matrix, but we just set that to an I-matrix
            // because we're already in CIRS.
            return eraC2tcio(IDENTITY, era, pm)
        }
    }
}
