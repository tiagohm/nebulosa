package nebulosa.nova.astrometry

import nebulosa.constants.TAU
import nebulosa.erfa.PositionAndVelocity
import nebulosa.math.Angle
import nebulosa.math.Angle.Companion.rad
import nebulosa.math.Distance
import nebulosa.math.Matrix3D
import nebulosa.math.Vector3D
import nebulosa.time.InstantOfTime
import kotlin.math.*

data class KeplerOrbit(
    val position: Vector3D,
    val velocity: Vector3D,
    val epoch: InstantOfTime,
    val mu: Double,
    override val center: Int,
    override val target: Int,
    val rotation: Matrix3D? = null,
) : Body {

    override fun compute(time: InstantOfTime): PositionAndVelocity {
        val (position, velocity) = propagate(position, velocity, epoch.tt, time.tt, mu)

        return if (rotation != null) {
            PositionAndVelocity(rotation * position, rotation * velocity)
        } else {
            PositionAndVelocity(position, velocity)
        }
    }

    @Suppress("LocalVariableName")
    companion object {

        private const val LN_1_5 = 0.4054651081081644
        private const val LN_HALF_DOUBLE_MAX = 709.0895657128241
        private const val LN_DOUBLE_MAX = 709.782712893384

        /**
         * Creates a [KeplerOrbit] from orbital elements using mean anomaly.
         */
        @JvmStatic
        internal fun meanAnomaly(
            semilatusRectum: Distance,
            eccentricity: Double,
            inclination: Angle,
            longitudeOfAscendingNode: Angle,
            argumentOfPerihelion: Angle,
            meanAnomaly: Angle,
            epoch: InstantOfTime,
            mu: Double,
            center: Int = -1,
            target: Int = -1,
            rotation: Matrix3D? = null,
        ): KeplerOrbit {
            val trueAnomaly = when {
                eccentricity < 1.0 -> trueAnomalyClosed(eccentricity, eccentricAnomaly(eccentricity, meanAnomaly))
                eccentricity > 1.0 -> trueAnomalyHyperbolic(eccentricity, eccentricAnomaly(eccentricity, meanAnomaly))
                else -> trueAnomalyParabolic(semilatusRectum, mu, meanAnomaly)
            }

            return trueAnomaly(
                semilatusRectum,
                eccentricity,
                inclination,
                longitudeOfAscendingNode,
                argumentOfPerihelion,
                trueAnomaly,
                epoch,
                mu,
                center, target,
                rotation,
            )
        }

        /**
         * Creates a [KeplerOrbit] from orbital elements using true anomaly.
         */
        @JvmStatic
        internal fun trueAnomaly(
            semilatusRectum: Distance,
            eccentricity: Double,
            inclination: Angle,
            longitudeOfAscendingNode: Angle,
            argumentOfPerihelion: Angle,
            trueAnomaly: Angle,
            epoch: InstantOfTime,
            mu: Double,
            center: Int = -1,
            target: Int = -1,
            rotation: Matrix3D? = null,
        ): KeplerOrbit {
            val (position, velocity) = computeOrbitalElementsAsVector(
                semilatusRectum,
                eccentricity,
                inclination,
                longitudeOfAscendingNode,
                argumentOfPerihelion,
                trueAnomaly,
                mu,
            )

            return KeplerOrbit(
                position, velocity,
                epoch,
                mu,
                center, target,
                rotation,
            )
        }

        /**
         * Iterates to solve Kepler's equation to find eccentric anomaly.
         *
         * Based on the algorithm in section 8.10.2 of the Explanatory Supplement
         * to the Astronomical Almanac, 3rd ed.
         */
        @JvmStatic
        fun eccentricAnomaly(e: Double, M: Angle): Angle {
            val m = (M + PI) % TAU - PI
            var E = m + e * m.sin

            for (i in 0..99) {
                val dM = m - (E - e * E.sin)
                val dE = dM / (1 - e * E.cos)
                E += dE
                if (abs(dE.value) < 1e-14) break
            }

            return E
        }

        /**
         * Computes true anomaly from eccentricity [e] and eccentric anomaly [E].
         *
         * Valid for hyperbolic orbits.
         */
        @JvmStatic
        fun trueAnomalyHyperbolic(e: Double, E: Angle) = (2.0 * atan(sqrt((e + 1.0) / (e - 1.0)) * tanh(E.value / 2.0))).rad

        /**
         * Computes true anomaly from eccentricity [e] and eccentric anomaly [E].
         *
         * Valid for closed orbits.
         */
        @JvmStatic
        fun trueAnomalyClosed(e: Double, E: Angle) = (2.0 * atan(sqrt((1.0 + e) / (1.0 - e)) * tan(E.value / 2.0))).rad

        /**
         * Computes the true anomaly from semi-latus rectum [p], [mu], and mean anomaly [M].
         *
         * Valid for parabolic orbits.
         */
        @JvmStatic
        fun trueAnomalyParabolic(
            p: Distance,
            mu: Double,
            M: Angle,
        ): Angle {
            // From http://www.bogan.ca/orbits/kepler/orbteqtn.html
            val dt = sqrt(2.0 * (p.value * p.value * p.value) / mu) * M.value
            val periapsis = p / 2.0
            val a = 1.5 * sqrt(mu / (2.0 * (periapsis.value * periapsis.value * periapsis.value))) * dt
            val b = cbrt(a + (a * a + 1.0))
            return (2.0 * atan(b - 1.0 / b)).rad
        }

        /**
         * Computes the state vectors from orbital elements.
         * Based on equations from this document:
         *
         * http://ccar.colorado.edu/asen5070/handouts/kep2cart_2002.doc
         *
         * @param p Semilatus Rectum
         */
        @JvmStatic
        fun computeOrbitalElementsAsVector(
            p: Distance,
            e: Double,
            i: Angle, om: Angle, w: Angle, v: Angle,
            mu: Double,
        ): PositionAndVelocity {
            // Checks that true anomaly is less than arccos(-1/e) for hyperbolic orbits.
            if (e > 1 && v.value > acos(-1.0 / e)) {
                throw IllegalArgumentException("if eccentricity is > 1, abs(true anomaly) cannot be more than acos(-1/e)")
            }

            val r = p.value / (1 + e * v.cos)
            val h = sqrt(p.value * mu)
            val u = v + w

            val cosOm = om.cos
            val sinOm = om.sin
            val cosu = u.cos
            val sinu = u.sin
            val cosi = i.cos
            val sini = i.sin
            val sinv = v.sin

            val x = r * (cosOm * cosu - sinOm * sinu * cosi)
            val y = r * (sinOm * cosu + cosOm * sinu * cosi)
            val z = r * (sini * sinu)

            val xDot = x * h * e / (r * p.value) * sinv - h / r * (cosOm * sinu + sinOm * cosu * cosi)
            val yDot = y * h * e / (r * p.value) * sinv - h / r * (sinOm * sinu - cosOm * cosu * cosi)
            val zDot = z * h * e / (r * p.value) * sinv + h / r * sini * cosu

            return PositionAndVelocity(Vector3D(x, y, z), Vector3D(xDot, yDot, zDot))
        }

        /**
         * Propagates a [position] and [velocity] vector over time.
         *
         * @param position Position in km.
         * @param velocity Velocity in km/s.
         * @param t0 [InstantOfTime] corresponding to [position] and [velocity].
         * @param t1 [InstantOfTime] to propagate to.
         * @param mu Gravitational parameter in units that match the other arguments.
         */
        @JvmStatic
        private fun propagate(
            position: Vector3D, velocity: Vector3D,
            t0: InstantOfTime, t1: InstantOfTime,
            mu: Double,
        ): PositionAndVelocity {
            val r0 = position.length
            val rv = position.dot(velocity)

            val hvec = position.cross(velocity)
            val h2 = hvec.dot(hvec)

            require(h2 != 0.0) { "motion is not conical" }

            val eqvec = velocity.cross(hvec) / mu - position / r0
            val e = eqvec.length
            val q = h2 / (mu * (1.0 + e))

            val f = 1 - e
            val b = sqrt(q / mu)

            val br0 = b * r0
            val b2rv = b * b * rv
            val bq = b * q
            val qovr0 = q / r0

            val hyperbolic = f < 0.0
            val maxc = max(abs(br0), max(abs(b2rv), max(abs(bq), abs(qovr0))))

            val bound = if (hyperbolic) {
                val fixed = LN_HALF_DOUBLE_MAX - ln(maxc)
                val root = sqrt(-f)
                min(fixed / root, (fixed + 1.5 * ln(-f)) / root)
            } else {
                exp((LN_1_5 + LN_DOUBLE_MAX - ln(maxc)) / 3)
            }

            fun kepler(x: Double): Double {
                val (_, c1, c2, c3) = stumpff(f * x * x)
                return x * (br0 * c1 + x * (b2rv * c2 + x * bq * c3))
            }

            val dt = t1.whole - t0.whole + t1.fraction - t0.fraction // T1 - T0
            var x = max(-bound, min(dt / bq, bound))

            var kfun = kepler(x)

            var lower = if (dt < 0) x else 0.0
            var upper = if (dt > 0) x else 0.0

            while (dt < 0.0 && kfun > dt) {
                upper = lower

                lower *= 2.0

                val oldx = x
                x = max(-bound, min(lower, bound))
                if (x == oldx) throw IllegalStateException("The delta time $dt is beyond the range")

                kfun = kepler(x)
            }

            while (dt > 0.0 && kfun < dt) {
                lower = upper

                upper *= 2

                val oldx = x
                x = max(-bound, min(upper, bound))
                if (x == oldx) throw IllegalStateException("The delta time $dt is beyond the range")

                kfun = kepler(x)
            }

            x = if (lower <= upper) (upper + lower) / 2.0 else upper

            var count = 64

            while (count-- > 0 && lower < x && x < upper) {
                kfun = kepler(x)

                if (kfun >= dt) upper = x
                if (kfun <= dt) lower = x

                x = (upper + lower) / 2.0
            }

            val (c0, c1, c2, c3) = stumpff(f * x * x)
            val br = br0 * c0 + x * (b2rv * c1 + x * bq * c2)

            val pc = 1.0 - qovr0 * x * x * c2
            val vc = dt - bq * x * x * x * c3
            val pcdot = -qovr0 / br * x * c1
            val vcdot = 1.0 - bq / br * x * x * c2

            val pos = position * pc + velocity * vc
            val vel = position * pcdot + velocity * vcdot

            return PositionAndVelocity(pos, vel)
        }

        @JvmStatic
        private val ODD_FACTORIALS = doubleArrayOf(
            6.0,
            120.0,
            5040.0,
            362880.0,
            39916800.0,
            6227020800.0,
            1307674368000.0,
            355687428096000.0,
            121645100408832000.0,
        )

        @JvmStatic
        private val EVEN_FACTORIALS = doubleArrayOf(
            2.0,
            24.0,
            720.0,
            40320.0,
            3628800.0,
            479001600.0,
            87178291200.0,
            20922789888000.0,
            6402373705728000.0,
        )

        /**
         * Computes Stumpff functions.
         *
         * Based on the function toolkit/src/spicelib/stmp03.f from the SPICE toolkit.
         */
        @JvmStatic
        private fun stumpff(x: Double): DoubleArray {
            val z = sqrt(abs(x))

            var c0 = if (x < -1.0) cosh(z) else if (x > 1.0) cos(z) else x
            var c1 = if (x < -1.0) sinh(z) / z else if (x > 1.0) sin(z) / z else x
            var c2 = 0.0
            var c3 = 0.0

            if (x in -1.0..1.0) {
                for (i in 0..8) {
                    val n = if (i % 2 == 0) x else -x
                    val k = n.pow(i)
                    c3 += k / ODD_FACTORIALS[i]
                    c2 += k / EVEN_FACTORIALS[i]
                }

                c0 = 1.0 - x * c2
                c1 = 1.0 - x * c3
            } else {
                c2 = (1.0 - c0) / x
                c3 = (1.0 - c1) / x
            }

            return doubleArrayOf(c0, c1, c2, c3)
        }
    }
}
