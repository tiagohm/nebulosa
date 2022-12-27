package nebulosa.nova.astrometry

import nebulosa.constants.DAYSPERJM
import nebulosa.constants.J2000
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
    private val terms = Array(6) { Array(3) { DoubleArray(0) } }

    init {
        val buffer = bufferedResource("VSOP87E_$name.txt")!!

        var xyz = 0
        var exp = 0

        while (!buffer.exhausted()) {
            val line = buffer.readUtf8Line()?.trimStart() ?: break

            if (line.startsWith("VSOP87")) {
                xyz = line[40].code - 49
                exp = line[58].code - 48
                val size = line.substring(59..65).trim().toInt()
                terms[exp][xyz] = DoubleArray(size * 3)
                continue
            }

            val index = (line.substring(4..8).trim().toInt() - 1) * 3
            val a = line.substring(78..95).trim().toDouble()
            val b = line.substring(96..109).trim().toDouble()
            val c = line.substring(110..129).trim().toDouble()
            terms[exp][xyz][index] = a
            terms[exp][xyz][index + 1] = b
            terms[exp][xyz][index + 2] = c
        }

        buffer.close()
    }

    override val center = 0 // SSB.

    override fun compute(time: InstantOfTime): Pair<Vector3D, Vector3D> {
        val t = DoubleArray(6)
        t[0] = 1.0
        t[1] = (time.tdb.whole - J2000 + time.tdb.fraction) / DAYSPERJM
        for (i in 2..5) t[i] = t[i - 1] * t[1]

        val p = DoubleArray(3)
        val v = DoubleArray(3)

        for (k in 0..2) {
            for (e in 0..5) {
                var psum = 0.0

                val terms = terms[e][k]

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

        return REFERENCE_FRAME * Vector3D(p) to REFERENCE_FRAME * Vector3D(v)
    }

    companion object {

        /**
         * The coordinates of the main version VSOP87 and of the version A, B, and E are
         * are given in the inertial frame defined by the dynamical equinox and ecliptic
         * J2000 (JD2451545.0).
         */
        @JvmStatic private val REFERENCE_FRAME = Matrix3D(
            1.000000000000, 0.000000440360, -0.000000190919,
            -0.000000479966, 0.917482137087, -0.397776982902,
            0.000000000000, 0.397776982902, 0.917482137087,
        )
    }
}
