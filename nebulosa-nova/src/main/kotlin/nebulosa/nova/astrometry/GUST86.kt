package nebulosa.nova.astrometry

import nebulosa.constants.AU_KM
import nebulosa.constants.DAYSEC
import nebulosa.constants.DAYSPERJY
import nebulosa.constants.DEG2RAD
import nebulosa.erfa.PositionAndVelocity
import nebulosa.math.Matrix3D
import nebulosa.math.Vector3D
import nebulosa.time.InstantOfTime
import kotlin.math.*

enum class GUST86(override val target: Int) : Body {
    ARIEL(701) {
        override val aeSeries = doubleArrayOf(-3.35e-6, 1187.63e-6, 861.59e-6, 71.50e-6, 55.59e-6)
        override val aiSeries = doubleArrayOf(-121.75e-6, 358.25e-06, 290.08e-06, 97.78e-06, 33.97e-06)
        override val amplitudes = doubleArrayOf(-84.60e-06, +91.81e-06, +20.03e-06, +89.77e-06)

        override fun computeOrbitalElements(
            t: Double,
            elems: DoubleArray,
            an: DoubleArray, ae: DoubleArray, ai: DoubleArray,
        ) {
            elems[0] =
                2492542.57e-06 + 2.55e-06 * cos(an[0] - 3.0 * an[1] + 2.0 * an[2]) - 42.16e-06 * cos(an[1] - an[2]) - 102.56e-06 * cos(2.0 * an[1] - 2.0 * an[2])
            elems[1] =
                3098046.41e-06 + 2492952.52e-06 * t - 1860.50e-06 * sin(an[0] - 3.0 * an[1] + 2.0 * an[2]) + 219.99e-06 * sin(2.0 * an[0] - 6.0 * an[1] + 4.0 * an[2]) + 23.10e-06 * sin(
                    3.0 * an[0] - 9.0 * an[1] + 6.0 * an[2]
                ) + 4.30e-06 * sin(4.0 * an[0] - 12.0 * an[1] + 8.0 * an[2]) - 90.11e-06 * sin(an[1] - an[2]) - 91.07e-06 * sin(2.0 * an[1] - 2.0 * an[2]) - 42.75e-06 * sin(
                    3.0 * an[1] - 3.0 * an[2]
                ) - 16.49e-06 * sin(2.0 * an[1] - 2.0 * an[3])

            val phases = DoubleArray(amplitudes.size)
            phases[0] = 2.0 * an[2] - an[1]
            phases[1] = 3.0 * an[2] - 2.0 * an[1]
            phases[2] = 2.0 * an[3] - an[1]
            phases[3] = an[1]

            computeSeries(elems, ae, ai, phases)
        }
    },
    UMBRIEL(702) {
        override val aeSeries = doubleArrayOf(-0.21e-6, -227.95e-6, 3904.69e-6, 309.17e-6, 221.92e-6)
        override val aiSeries = doubleArrayOf(-10.86e-6, -81.51e-06, 1113.36e-06, 350.14e-06, 106.50e-06)
        override val amplitudes = doubleArrayOf(
            29.34e-6, 26.20e-6, 51.19e-6, -103.86e-6, -27.16e-6, -16.22e-6, 549.23e-6, 34.70e-6, 12.81e-6, 21.81e-6,
            46.25e-6,
        )

        override fun computeOrbitalElements(
            t: Double,
            elems: DoubleArray,
            an: DoubleArray, ae: DoubleArray, ai: DoubleArray,
        ) {
            elems[0] =
                1515954.90e-06 + 9.74e-06 * cos(an[2] - 2.0 * an[3] + ae[2]) - 106.00e-06 * cos(an[1] - an[2]) + 54.16e-06 * cos(2.0 * an[1] - 2.0 * an[2]) - 23.59e-06 * cos(
                    an[2] - an[3]
                ) - 70.70e-06 * cos(2.0 * an[2] - 2.0 * an[3]) - 36.28e-06 * cos(3.0 * an[2] - 3.0 * an[3])
            elems[1] =
                2285401.69e-06 + 1516148.11e-06 * t + 660.57e-06 * sin(an[0] - 3.0 * an[1] + 2.0 * an[2]) - 76.51e-06 * sin(2.0 * an[0] - 6.0 * an[1] + 4.0 * an[2]) - 8.96e-06 * sin(
                    3.0 * an[0] - 9.0 * an[1] + 6.0 * an[2]
                ) - 2.53e-06 * sin(4.0 * an[0] - 12.0 * an[1] + 8.0 * an[2]) - 52.91e-06 * sin(an[2] - 4.0 * an[3] + 3.0 * an[4]) - 7.34e-06 * sin(an[2] - 2.0 * an[3] + ae[4]) - 1.83e-06 * sin(
                    an[2] - 2.0 * an[3] + ae[3]
                ) + 147.91e-06 * sin(an[2] - 2.0 * an[3] + ae[2])
            elems[1] += -7.77e-06 * sin(an[2] - 2.0 * an[3] + ae[1]) + 97.76e-06 * sin(an[1] - an[2]) + 73.13e-06 * sin(2.0 * an[1] - 2.0 * an[2]) + 34.71e-06 * sin(
                3.0 * an[1] - 3.0 * an[2]
            ) + 18.89e-06 * sin(4.0 * an[1] - 4.0 * an[2]) - 67.89e-06 * sin(an[2] - an[3]) - 82.86e-06 * sin(2.0 * an[2] - 2.0 * an[3])
            elems[1] += -33.81e-06 * sin(3.0 * an[2] - 3.0 * an[3]) - 15.79e-06 * sin(4.0 * an[2] - 4.0 * an[3]) - 10.21e-06 * sin(an[2] - an[4]) - 17.08e-06 * sin(
                2.0 * an[2] - 2.0 * an[4]
            )

            val phases = DoubleArray(amplitudes.size)
            phases[0] = an[1]
            phases[1] = an[2]
            phases[2] = -an[1] + 2.0 * an[2]
            phases[3] = -2.0 * an[1] + 3.0 * an[2]
            phases[4] = -3.0 * an[1] + 4.0 * an[2]
            phases[5] = an[3]
            phases[6] = -an[2] + 2.0 * an[3]
            phases[7] = -2.0 * an[2] + 3.0 * an[3]
            phases[8] = -3.0 * an[2] + 4.0 * an[3]
            phases[9] = -an[2] + 2.0 * an[4]
            phases[10] = an[2]

            computeSeries(elems, ae, ai, phases)
        }
    },
    TITANIA(703) {
        override val aeSeries = doubleArrayOf(-0.02e-6, -1.29e-6, -324.51e-6, 932.81e-6, 1120.89e-6)
        override val aiSeries = doubleArrayOf(-1.43e-6, -1.06e-06, -140.13e-06, 685.72e-06, 378.32e-06)
        override val amplitudes = doubleArrayOf(
            33.86e-6, 17.46e-6, 16.58e-6, 28.89e-6, -35.86e-6,
            -17.86e-6, -32.10e-6, -177.83e-6, 793.43e-6, 99.48e-6,
            44.83e-6, 25.13e-6, 15.43e-6
        )

        override fun computeOrbitalElements(
            t: Double,
            elems: DoubleArray,
            an: DoubleArray, ae: DoubleArray, ai: DoubleArray,
        ) {
            elems[0] =
                721663.16e-06 - 2.64e-06 * cos(an[2] - 2.0 * an[3] + ae[2]) - 2.16e-06 * cos(2.0 * an[3] - 3.0 * an[4] + ae[4]) + 6.45e-06 * cos(2.0 * an[3] - 3.0 * an[4] + ae[3]) - 1.11e-06 * cos(
                    2.0 * an[3] - 3.0 * an[4] + ae[2]
                )
            elems[0] += -62.23e-06 * cos(an[1] - an[3]) - 56.13e-06 * cos(an[2] - an[3]) - 39.94e-06 * cos(an[3] - an[4]) - 91.85e-06 * cos(2.0 * an[3] - 2.0 * an[4]) - 58.31e-06 * cos(
                3.0 * an[3] - 3.0 * an[4]
            ) - 38.60e-06 * cos(4.0 * an[3] - 4.0 * an[4]) - 26.18e-06 * cos(5.0 * an[3] - 5.0 * an[4]) - 18.06e-06 * cos(6.0 * an[3] - 6.0 * an[4])
            elems[1] =
                856358.79e-06 + 721718.51e-06 * t + 20.61e-06 * sin(an[2] - 4.0 * an[3] + 3.0 * an[4]) - 2.07e-06 * sin(an[2] - 2.0 * an[3] + ae[4]) - 2.88e-06 * sin(
                    an[2] - 2.0 * an[3] + ae[3]
                ) - 40.79e-06 * sin(an[2] - 2.0 * an[3] + ae[2]) + 2.11e-06 * sin(an[2] - 2.0 * an[3] + ae[1]) - 51.83e-06 * sin(2.0 * an[3] - 3.0 * an[4] + ae[4]) + 159.87e-06 * sin(
                    2.0 * an[3] - 3.0 * an[4] + ae[3]
                )
            elems[1] += -35.05e-06 * sin(2.0 * an[3] - 3.0 * an[4] + ae[2]) - 1.56e-06 * sin(3.0 * an[3] - 4.0 * an[4] + ae[4]) + 40.54e-06 * sin(an[1] - an[3]) + 46.17e-06 * sin(
                an[2] - an[3]
            ) - 317.76e-06 * sin(an[3] - an[4]) - 305.59e-06 * sin(2.0 * an[3] - 2.0 * an[4]) - 148.36e-06 * sin(3.0 * an[3] - 3.0 * an[4]) - 82.92e-06 * sin(
                4.0 * an[3] - 4.0 * an[4]
            )
            elems[1] += -49.98e-06 * sin(5.0 * an[3] - 5.0 * an[4]) - 31.56e-06 * sin(6.0 * an[3] - 6.0 * an[4]) - 20.56e-06 * sin(7.0 * an[3] - 7.0 * an[4]) - 13.69e-06 * sin(
                8.0 * an[3] - 8.0 * an[4]
            )

            val phases = DoubleArray(amplitudes.size)
            phases[0] = an[1]
            phases[1] = an[3]
            phases[2] = -an[1] + 2.0 * an[3]
            phases[3] = an[2]
            phases[4] = -an[2] + 2.0 * an[3]
            phases[5] = an[3]
            phases[6] = an[4]
            phases[7] = -an[3] + 2.0 * an[4]
            phases[8] = -2.0 * an[3] + 3.0 * an[4]
            phases[9] = -3.0 * an[3] + 4.0 * an[4]
            phases[10] = -4.0 * an[3] + 5.0 * an[4]
            phases[11] = -5.0 * an[3] + 6.0 * an[4]
            phases[12] = -6.0 * an[3] + 7.0 * an[4]

            computeSeries(elems, ae, ai, phases)
        }
    },
    OBERON(704) {
        override val aeSeries = doubleArrayOf(0.00e-6, -0.35e-6, 74.53e-6, -758.68e-6, 1397.34e-6)
        override val aiSeries = doubleArrayOf(-0.44e-6, -0.31e-06, 36.89e-06, -596.33e-06, 451.69e-06)
        override val amplitudes = doubleArrayOf(
            39.00e-6, 17.66e-6, 32.42e-6, 79.75e-6, 75.66e-6, 134.04e-6,
            -987.26e-6, -126.09e-6, -57.42e-6, -32.41e-6, -19.99e-6, -12.94e-6
        )

        override fun computeOrbitalElements(
            t: Double,
            elems: DoubleArray,
            an: DoubleArray, ae: DoubleArray, ai: DoubleArray,
        ) {
            elems[0] =
                466580.54e-06 + 2.08e-06 * cos(2.0 * an[3] - 3.0 * an[4] + ae[4]) - 6.22e-06 * cos(2.0 * an[3] - 3.0 * an[4] + ae[3]) + 1.07e-06 * cos(
                    2.0 * an[3] - 3.0 * an[4] + ae[2]
                ) - 43.10e-06 * cos(an[1] - an[4])
            elems[0] += -38.94e-06 * cos(an[2] - an[4]) - 80.11e-06 * cos(an[3] - an[4]) + 59.06e-06 * cos(2.0 * an[3] - 2.0 * an[4]) + 37.49e-06 * cos(
                3.0 * an[3] - 3.0 * an[4]
            ) + 24.82e-06 * cos(4.0 * an[3] - 4.0 * an[4]) + 16.84e-06 * cos(5.0 * an[3] - 5.0 * an[4])
            elems[1] =
                -915591.80e-06 + 466692.12e-06 * t - 7.82e-06 * sin(an[2] - 4.0 * an[3] + 3.0 * an[4]) + 51.29e-06 * sin(2.0 * an[3] - 3.0 * an[4] + ae[4]) - 158.24e-06 * sin(
                    2.0 * an[3] - 3.0 * an[4] + ae[3]
                ) + 34.51e-06 * sin(2.0 * an[3] - 3.0 * an[4] + ae[2]) + 47.51e-06 * sin(an[1] - an[4]) + 38.96e-06 * sin(an[2] - an[4]) + 359.73e-06 * sin(
                    an[3] - an[4]
                )
            elems[1] += 282.78e-06 * sin(2.0 * an[3] - 2.0 * an[4]) + 138.60e-06 * sin(3.0 * an[3] - 3.0 * an[4]) + 78.03e-06 * sin(4.0 * an[3] - 4.0 * an[4]) + 47.29e-06 * sin(
                5.0 * an[3] - 5.0 * an[4]
            ) + 30.00e-06 * sin(6.0 * an[3] - 6.0 * an[4]) + 19.62e-06 * sin(7.0 * an[3] - 7.0 * an[4]) + 13.11e-06 * sin(8.0 * an[3] - 8.0 * an[4])

            val phases = DoubleArray(amplitudes.size)
            phases[0] = an[1]
            phases[1] = -an[1] + 2.0 * an[4]
            phases[2] = an[2]
            phases[3] = an[3]
            phases[4] = an[4]
            phases[5] = -an[3] + 2.0 * an[4]
            phases[6] = -2.0 * an[3] + 3.0 * an[4]
            phases[7] = -3.0 * an[3] + 4.0 * an[4]
            phases[8] = -4.0 * an[3] + 5.0 * an[4]
            phases[9] = -5.0 * an[3] + 6.0 * an[4]
            phases[10] = -6.0 * an[3] + 7.0 * an[4]
            phases[11] = -7.0 * an[3] + 8.0 * an[4]

            computeSeries(elems, ae, ai, phases)
        }
    },
    MIRANDA(705) {
        override val aeSeries = doubleArrayOf(1312.38e-6, 71.81e-6, 69.77e-6, 6.75e-6, 6.27e-6)
        override val aiSeries = doubleArrayOf(37871.71e-06, +27.01e-06, +30.76e-06, +12.18e-06, +5.37e-06)
        override val amplitudes = doubleArrayOf(-123.31e-6, 39.52e-6, 194.10e-6)

        override fun computeOrbitalElements(
            t: Double,
            elems: DoubleArray,
            an: DoubleArray, ae: DoubleArray, ai: DoubleArray,
        ) {
            elems[0] =
                4443522.67e-06 - 34.92e-06 * cos(an[0] - 3.0 * an[1] + 2.0 * an[2]) + 8.47e-06 * cos(2.0 * an[0] - 6.0 * an[1] + 4.0 * an[2]) + 1.31e-06 * cos(
                    3.0 * an[0] - 9.0 * an[1] + 6.0 * an[2]
                ) - 52.28e-06 * cos(an[0] - an[1]) - 136.65e-06 * cos(2.0 * an[0] - 2.0 * an[1])
            elems[1] =
                -238051.58e-06 + 4445190.55e-06 * t + 25472.17e-06 * sin(an[0] - 3.0 * an[1] + 2.0 * an[2]) - 3088.31e-06 * sin(2.0 * an[0] - 6.0 * an[1] + 4.0 * an[2]) - 318.10e-06 * sin(
                    3.0 * an[0] - 9.0 * an[1] + 6.0 * an[2]
                ) - 37.49e-06 * sin(4.0 * an[0] - 12.0 * an[1] + 8.0 * an[2]) - 57.85e-06 * sin(an[0] - an[1]) - 62.32e-06 * sin(2.0 * an[0] - 2.0 * an[1]) - 27.95e-06 * sin(
                    3.0 * an[0] - 3.0 * an[1]
                )

            val phases = DoubleArray(amplitudes.size)
            phases[0] = -an[0] + 2.0 * an[1]
            phases[1] = -2.0 * an[0] + 3.0 * an[1]
            phases[2] = an[0]

            computeSeries(elems, ae, ai, phases)
        }
    };

    override val center = 799 // Uranus.

    protected abstract val aeSeries: DoubleArray
    protected abstract val aiSeries: DoubleArray
    protected abstract val amplitudes: DoubleArray

    protected abstract fun computeOrbitalElements(
        t: Double,
        elems: DoubleArray,
        an: DoubleArray, ae: DoubleArray, ai: DoubleArray,
    )

    override fun compute(time: InstantOfTime): PositionAndVelocity {
        val td = time.tdb.whole - 2444239.5 + time.tdb.fraction
        val ty = td / DAYSPERJY

        val an = DoubleArray(5)
        val ae = DoubleArray(5)
        val ai = DoubleArray(5)

        for (i in 0..4) {
            an[i] = FQN[i] * td + PHN[i]
            ae[i] = FQE[i] * ty + PHE[i]
            ai[i] = FQI[i] * ty + PHI[i]
        }

        val elems = DoubleArray(6)

        computeOrbitalElements(td, elems, an, ae, ai)

        val rmu = GMU + GMS[ordinal]

        // From the above actually gives the mean motion, in radians
        // per day. Use Kepler's 3rd law to convert this to a semimajor
        // axis in kilometers.
        elems[0] = cbrt(rmu * DAYSEC * DAYSEC / (elems[0] * elems[0]))

        val (ra, rl, rk, rh, rq) = elems
        val rp = elems[5]
        val rn = sqrt(rmu / (ra * ra * ra))
        val phi = sqrt(1.0 - rk * rk - rh * rh)
        val rki = sqrt(1.0 - rq * rq - rp * rp)
        val psi = 1.0 / (1.0 + phi)

        val rot = Array(2) { DoubleArray(3) }
        rot[0][0] = 1.0 - 2 * rp * rp
        rot[1][0] = 2 * rp * rq
        rot[0][1] = 2 * rp * rq
        rot[1][1] = 1.0 - 2 * rq * rq
        rot[0][2] = -2.0 * rp * rki
        rot[1][2] = 2 * rq * rki

        val f = keplkh(rl, rk, rh)

        val sf = sin(f)
        val cf = cos(f)
        val rlmf = -rk * sf + rh * cf
        val umrsa = rk * cf + rh * sf
        val asr = 1.0 / (1.0 - umrsa)
        val rna2sr = rn * ra * asr

        val tx1 = DoubleArray(2)
        val tx1t = DoubleArray(2)

        tx1[0] = ra * (cf - psi * rh * rlmf - rk)
        tx1[1] = ra * (sf + psi * rk * rlmf - rh)
        tx1t[0] = rna2sr * (-sf + psi * rh * umrsa)
        tx1t[1] = rna2sr * (cf - psi * rk * umrsa)

        val xyz = DoubleArray(6)

        for (i in 0..2) {
            for (j in 0..1) {
                xyz[i] += rot[j][i] * tx1[j]
                xyz[i + 3] += rot[j][i] * tx1t[j]
            }
        }

        for (i in 0..2) xyz[i] /= AU_KM
        for (i in 3..5) xyz[i] *= DAYSEC / AU_KM

        return PositionAndVelocity(REFERENCE_FRAME * Vector3D(xyz), REFERENCE_FRAME * Vector3D(xyz, 3))
    }

    protected fun computeSeries(
        elems: DoubleArray, ae: DoubleArray,
        ai: DoubleArray, phases: DoubleArray,
    ) {
        for (i in 2..5) elems[i] = 0.0

        for (i in 0..4) {
            elems[2] += aeSeries[i] * cos(ae[i])
            elems[3] += aeSeries[i] * sin(ae[i])
            elems[4] += aiSeries[i] * cos(ai[i])
            elems[5] += aiSeries[i] * sin(ai[i])
        }

        for (i in phases.indices) {
            elems[2] += amplitudes[i] * cos(phases[i])
            elems[3] += amplitudes[i] * sin(phases[i])
        }
    }

    companion object {

        @JvmStatic private val FQN = doubleArrayOf(4445190.550e-06, 2492952.519e-06, 1516148.111e-06, 721718.509e-06, 466692.120e-06)
        @JvmStatic private val FQE = doubleArrayOf(20.082 * DEG2RAD, 6.217 * DEG2RAD, 2.865 * DEG2RAD, 2.078 * DEG2RAD, 0.386 * DEG2RAD)
        @JvmStatic private val FQI = doubleArrayOf(-20.309 * DEG2RAD, -6.288 * DEG2RAD, -2.836 * DEG2RAD, -1.843 * DEG2RAD, -0.259 * DEG2RAD)
        @JvmStatic private val PHN = doubleArrayOf(-238051.0e-06, 3098046.0e-06, 2285402.0e-06, 856359.0e-06, -915592.0e-06)
        @JvmStatic private val PHE = doubleArrayOf(0.611392, 2.408974, 2.067774, 0.735131, 0.426767)
        @JvmStatic private val PHI = doubleArrayOf(5.702313, 0.395757, 0.589326, 1.746237, 4.206896)

        private const val GMU = 5794950.5
        @JvmStatic private val GMS = doubleArrayOf(4.4, 86.1, 84.0, 230.0, 200.0)

        // private const val ALF = 76.60666666666667 * DEG2RAD
        // private const val DEL = 15.03222222222222 * DEG2RAD

        @Suppress("FloatingPointLiteralPrecision")
        @JvmStatic private val REFERENCE_FRAME = Matrix3D(
            0.975320689787805506, 0.061943212277559903, 0.211925908266559604,
            -0.220742291478488117, 0.252990568240833158, 0.941949368633859696,
            0.004732113777988428, -0.965483718541726765, 0.260420422133248453,
        )

        @JvmStatic
        private fun keplkh(rl: Double, rk: Double, rh: Double): Double {
            if (rl == 0.0) return 0.0

            var f0 = rl
            var e0 = abs(rl)
            var f = 0.0

            for (i in 0..19) {
                var k = 0
                val sf = sin(f0)
                val cf = cos(f0)
                var e: Double
                val ff0 = f0 - rk * sf + rh * cf - rl
                val fpf0 = 1.0 - rk * cf - rh * sf
                var sdirOver2ToTheKth = ff0 / fpf0

                do {
                    f = f0 - sdirOver2ToTheKth
                    e = abs(f - f0)

                    if (e > e0) {
                        k++
                        sdirOver2ToTheKth *= 0.5
                    }
                } while (e > e0)

                if (k == 0 && e <= 1e-16 && ff0 <= 1e-16) {
                    break
                } else {
                    f0 = f
                    e0 = e
                }
            }

            return f
        }
    }
}
