package nebulosa.nova.frame

import nebulosa.erfa.eraPnm06a
import nebulosa.math.Matrix3D
import nebulosa.time.InstantOfTime

class TrueEclipticRotationMatrix(val time: InstantOfTime) : Matrix3D(compute(time)) {

    companion object {

        @JvmStatic
        private fun compute(time: InstantOfTime): Matrix3D {
            val rnpb = eraPnm06a(time.tt.whole, time.tt.fraction)
            return rnpb.rotateX(time.trueObliquity)
        }
    }
}
