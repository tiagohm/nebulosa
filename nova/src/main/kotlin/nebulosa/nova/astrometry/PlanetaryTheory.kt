package nebulosa.nova.astrometry

import nebulosa.constants.AU_KM
import nebulosa.constants.TAU
import nebulosa.math.Vector3D
import kotlin.math.*

abstract class PlanetaryTheory {

    protected fun ellipticToRectangularA(mu: Double, elem: DoubleArray, dt: Double): Pair<Vector3D, Vector3D> {
        val a = elem[0]
        val n = sqrt(mu / (a * a * a)) // Mean motion
        return ellipticToRectangular(a, n, elem, dt)
    }

    @OptIn(ExperimentalStdlibApi::class)
    protected fun ellipticToRectangularN(mu: Double, elem: DoubleArray, dt: Double): Pair<Vector3D, Vector3D> {
        val n = elem[0]
        val a = cbrt(mu / (n * n))
        return ellipticToRectangular(a, n, elem, dt)
    }

    /**
     * Given the orbital elements at some time t0 calculate the
     * rectangular coordinates at time (t0+dt).
     *
     * mu = G*(m1+m2) .. gravitational constant of the two body problem
     * @param a Semi major axis
     * @param n Mean motion = 2*M_PI/(orbit period)
     */
    @Suppress("LocalVariableName")
    protected fun ellipticToRectangular(a: Double, n: Double, elem: DoubleArray, dt: Double): Pair<Vector3D, Vector3D> {
        val L = (elem[1] + n * dt) % TAU
        var Le = L - elem[2] * sin(L) + elem[3] * cos(L)

        while (true) {
            val cLe = cos(Le)
            val sLe = sin(Le)

            // For excentricity < 1 we have denominator > 0
            val dLe = (L - Le + elem[2] * sLe - elem[3] * cLe) / (1.0 - elem[2] * cLe - elem[3] * sLe)
            Le += dLe

            if (abs(dLe) <= 1e-12) break
        }

        val cLe = cos(Le)
        val sLe = sin(Le)

        val dlf = -elem[2] * sLe + elem[3] * cLe
        val phi = sqrt(1.0 - elem[2] * elem[2] - elem[3] * elem[3])
        val psi = 1.0 / (1.0 + phi)

        val x1 = a * (cLe - elem[2] - psi * dlf * elem[3])
        val y1 = a * (sLe - elem[3] + psi * dlf * elem[2])

        val elem4q = elem[4] * elem[4] // Q²
        val elem5q = elem[5] * elem[5] // P²
        val dwho = 2.0 * sqrt(1.0 - elem4q - elem5q)
        val rtp = 1.0 - elem5q - elem5q
        val rtq = 1.0 - elem4q - elem4q
        val rdg = 2.0 * elem[5] * elem[4]

        val xyz = DoubleArray(6)

        xyz[0] = x1 * rtp + y1 * rdg
        xyz[1] = x1 * rdg + y1 * rtq
        xyz[2] = (-x1 * elem[5] + y1 * elem[4]) * dwho

        val rsam1 = -elem[2] * cLe - elem[3] * sLe
        val h = a * n / (1.0 + rsam1)
        val vx1 = h * (-sLe - psi * rsam1 * elem[3])
        val vy1 = h * (cLe + psi * rsam1 * elem[2])

        xyz[3] = vx1 * rtp + vy1 * rdg
        xyz[4] = vx1 * rdg + vy1 * rtq
        xyz[5] = (-vx1 * elem[5] + vy1 * elem[4]) * dwho

        return Vector3D(xyz) to Vector3D(xyz, 3)
    }

    protected fun computeInterpolatedElements(
        t: Double,
        elem: DoubleArray,
        dim: Int,
        computer: (Double, DoubleArray) -> Unit,
        deltaT: Double,
        ts: DoubleArray,
        es: Array<DoubleArray>,
    ) {
        if (ts[1] < -1E+99) {
            ts[0] = -1E+100
            ts[2] = -1E+100
            ts[1] = t
            computer(ts[1], es[1])
            for (i in 0 until dim) elem[i] = es[1][i]
            return
        }

        if (t <= ts[1]) {
            if (ts[1] - deltaT <= t) {
                if (ts[0] < -1E+99) {
                    ts[0] = ts[1] - deltaT
                    computer(ts[0], es[0])
                }
            } else if (ts[1] - 2.0 * deltaT <= t) {
                if (ts[0] < -1E+99) {
                    ts[0] = ts[1] - deltaT
                    computer(ts[0], es[0])
                }

                ts[2] = ts[1];ts[1] = ts[0]

                for (i in 0 until dim) {
                    es[2][i] = es[1][i]
                    es[1][i] = es[0][i]
                }

                ts[0] = ts[1] - deltaT

                computer(ts[0], es[0])
            } else {
                ts[0] = -1E+100
                ts[2] = -1E+100
                ts[1] = t

                computer(ts[1], es[1])

                for (i in 0 until dim) elem[i] = es[1][i]

                return
            }

            val f0 = (ts[1] - t)
            val f1 = (t - ts[0])
            val fact = 1.0 / deltaT

            for (i in 0 until dim) elem[i] = fact * (es[0][i] * f0 + es[1][i] * f1)
        } else {
            if (ts[1] + deltaT >= t) {
                if (ts[2] < -1E+99) {
                    ts[2] = ts[1] + deltaT
                    computer(ts[2], es[2])
                }
            } else if (ts[1] + 2.0 * deltaT >= t) {
                if (ts[2] < -1E+99) {
                    ts[2] = ts[1] + deltaT
                    computer(ts[2], es[2])
                }

                ts[0] = ts[1]
                ts[1] = ts[2]

                for (i in 0 until dim) {
                    es[0][i] = es[1][i]
                    es[1][i] = es[2][i]
                }

                ts[2] = ts[1] + deltaT

                computer(ts[2], es[2])
            } else {
                ts[0] = -1E+100
                ts[2] = -1E+100
                ts[1] = t

                computer(ts[1], es[1])

                for (i in 0 until dim) elem[i] = es[1][i]

                return
            }

            val f1 = (ts[2] - t)
            val f2 = (t - ts[1])
            val fact = 1.0 / deltaT

            for (i in 0 until dim) elem[i] = fact * (es[1][i] * f1 + es[2][i] * f2)
        }
    }
}
