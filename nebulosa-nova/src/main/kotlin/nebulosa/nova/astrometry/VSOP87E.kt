package nebulosa.nova.astrometry

import nebulosa.constants.DAYSPERJM
import nebulosa.constants.J2000
import nebulosa.erfa.PositionAndVelocity
import nebulosa.io.bufferedResource
import nebulosa.math.Matrix3D
import nebulosa.math.Vector3D
import nebulosa.time.InstantOfTime
import kotlin.math.cos
import kotlin.math.sin

enum class VSOP87E(override val target: Int) : Body {
    SUN(10),
    MERCURY(199),
    VENUS(299),
    EARTH(399),
    MARS(499),
    JUPITER(599),
    SATURN(699),
    URANUS(799),
    NEPTUNE(899);

    // Exponent, XYZ, terms.
    private val terms by lazy { bufferedResource("VSOP87E_$name.dat")!!.use(VSOP87EReader::readBinaryFormat) }

    override val center = 0 // SSB.

    override fun compute(time: InstantOfTime): PositionAndVelocity {
        val t = DoubleArray(6)
        t[0] = 1.0
        t[1] = (time.tdb.whole - J2000 + time.tdb.fraction) / DAYSPERJM
        for (i in 2..5) t[i] = t[i - 1] * t[1]

        val p = DoubleArray(3)
        val v = DoubleArray(3)

        val data = terms

        for (k in 0..2) {
            for (e in 0..5) {
                var psum = 0.0

                val terms = data[e][k]

                for (i in terms.indices step 3) {
                    val a = terms[i]
                    val b = terms[i + 1]
                    val c = terms[i + 2]

                    val u = b + c * t[1]
                    val j = a * cos(u)

                    psum += j
                    v[k] += (if (e > 0) t[e - 1] * e * j else 0.0) - t[e] * a * c * sin(u)
                }

                p[k] += psum * t[e]
            }

            v[k] /= DAYSPERJM
        }

        return PositionAndVelocity(REFERENCE_FRAME * Vector3D(p), REFERENCE_FRAME * Vector3D(v))
    }

    companion object {

        /**
         * The coordinates of the main version VSOP87 and of the version A, B, and E are
         * are given in the inertial frame defined by the dynamical equinox and ecliptic
         * J2000 (JD2451545.0).
         */
        private val REFERENCE_FRAME = Matrix3D(
            1.000000000000, 0.000000440360, -0.000000190919,
            -0.000000479966, 0.917482137087, -0.397776982902,
            0.000000000000, 0.397776982902, 0.917482137087,
        )
    }
}
