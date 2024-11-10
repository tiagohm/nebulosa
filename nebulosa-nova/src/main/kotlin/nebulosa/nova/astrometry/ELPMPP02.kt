package nebulosa.nova.astrometry

import nebulosa.constants.*
import nebulosa.erfa.PositionAndVelocity
import nebulosa.io.bufferedResource
import nebulosa.math.Matrix3D
import nebulosa.math.Vector3D
import nebulosa.math.normalized
import nebulosa.time.InstantOfTime
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Accurate Moon positions using the Lunar solution ELP/MPP02.
 *
 * @see <a href="ftp://cyrano-se.obspm.fr/pub/2_lunar_solutions/2_elpmpp02">Fortran</a>
 * @see <a href="https://github.com/timmyd7777/SSCore/blob/master/SSCode/VSOP2013/ELPMPP02.cpp">C++</a>
 */
data object ELPMPP02 : Body {

    private val main = Array(3) { readMainProblemFile(it + 1) }
    private val pert = Array(3) { readPertubationFile(it + 1) }

    override val center = 399 // Earth.

    override val target = 301 // Moon.

    @Suppress("UnnecessaryVariable")
    override fun compute(time: InstantOfTime): PositionAndVelocity {
        val t = DoubleArray(5)
        t[0] = 1.0
        t[1] = (time.tdb.whole - J2000 + time.tdb.fraction) / DAYSPERJC
        for (i in 2..4) t[i] = t[i - 1] * t[1]

        val v = DoubleArray(6)

        for (iv in 0..2) {
            val (cmpb, fmpb) = main[iv]

            for (n in cmpb.indices) {
                val x = cmpb[n]
                var y = fmpb[n][0]
                var yp = 0.0

                for (k in 1..4) {
                    y += fmpb[n][k] * t[k]
                    yp += k * fmpb[n][k] * t[k - 1]
                }

                v[iv] += x * sin(y)
                v[iv + 3] += x * yp * cos(y)
            }

            for (it in pert[iv].indices) {
                val (cper, fper) = pert[iv][it]

                for (n in cper.indices) {
                    val x = cper[n]
                    var y = fper[n][0]
                    var xp = 0.0
                    var yp = 0.0

                    if (it != 0) xp = it * x * t[it - 1]

                    for (k in 1..4) {
                        y += fper[n][k] * t[k]
                        yp += k * fper[n][k] * t[k - 1]
                    }

                    v[iv] += x * t[it] * sin(y)
                    v[iv + 3] += xp * sin(y) + x * t[it] * yp * cos(y)
                }
            }
        }

        v[0] = v[0] * ASEC2RAD + W10 + W11 * t[1] + W12 * t[2] + W13 * t[3] + W14 * t[4]
        v[1] = v[1] * ASEC2RAD
        v[2] = v[2] * RA0
        v[3] = v[3] * ASEC2RAD + W11 + 2.0 * W12 * t[1] + 3.0 * W13 * t[2] + 4.0 * W14 * t[3]
        v[4] = v[4] * ASEC2RAD

        val clamb = cos(v[0])
        val slamb = sin(v[0])
        val cbeta = cos(v[1])
        val sbeta = sin(v[1])
        val cw = v[2] * cbeta
        val sw = v[2] * sbeta

        val x1 = cw * clamb
        val x2 = cw * slamb
        val x3 = sw
        val xp1 = (v[5] * cbeta - v[4] * sw) * clamb - v[3] * x2
        val xp2 = (v[5] * cbeta - v[4] * sw) * slamb + v[3] * x1
        val xp3 = v[5] * sbeta + v[4] * cw

        val pw = (P1 + P2 * t[1] + P3 * t[2] + P4 * t[3] + P5 * t[4]) * t[1]
        val qw = (Q1 + Q2 * t[1] + Q3 * t[2] + Q4 * t[3] + Q5 * t[4]) * t[1]
        val ra = 2.0 * sqrt(1 - pw * pw - qw * qw)
        val pwqw = 2.0 * pw * qw
        val pw2 = 1.0 - 2.0 * pw * pw
        val qw2 = 1.0 - 2.0 * qw * qw
        val pwra = pw * ra
        val qwra = qw * ra

        v[0] = (pw2 * x1 + pwqw * x2 + pwra * x3) / AU_KM
        v[1] = (pwqw * x1 + qw2 * x2 - qwra * x3) / AU_KM
        v[2] = (-pwra * x1 + qwra * x2 + (pw2 + qw2 - 1) * x3) / AU_KM

        val ppw = P1 + (2.0 * P2 + 3.0 * P3 * t[1] + 4.0 * P4 * t[2] + 5.0 * P5 * t[3]) * t[1]
        val qpw = Q1 + (2.0 * Q2 + 3.0 * Q3 * t[1] + 4.0 * Q4 * t[2] + 5.0 * Q5 * t[3]) * t[1]
        val ppw2 = -4.0 * pw * ppw
        val qpw2 = -4.0 * qw * qpw
        val ppwqpw = 2.0 * (ppw * qw + pw * qpw)
        val rap = (ppw2 + qpw2) / ra
        val ppwra = ppw * ra + pw * rap
        val qpwra = qpw * ra + qw * rap

        v[3] = (pw2 * xp1 + pwqw * xp2 + pwra * xp3 + ppw2 * x1 + ppwqpw * x2 + ppwra * x3) / DAYSPERJC / AU_KM
        v[4] = (pwqw * xp1 + qw2 * xp2 - qwra * xp3 + ppwqpw * x1 + qpw2 * x2 - qpwra * x3) / DAYSPERJC / AU_KM
        v[5] = (-pwra * xp1 + qwra * xp2 + (pw2 + qw2 - 1.0) * xp3 - ppwra * x1 + qpwra * x2 + (ppw2 + qpw2) * x3) / DAYSPERJC / AU_KM

        return PositionAndVelocity(REFERENCE_FRAME * Vector3D(v), REFERENCE_FRAME * Vector3D(v, 3))
    }

    private val REFERENCE_FRAME = Matrix3D(
        1.000000000000, 0.000000440360, -0.000000190919,
        -0.000000479966, 0.917482137087, -0.397776982902,
        0.000000000000, 0.397776982902, 0.917482137087,
    )

    private const val RA0 = 384747.961370173 / 384747.980674318

    // Constant for the correction to the constant of precession - source: IAU 2000A.
    private const val DPREC = -0.29965

    private const val BP11 = 0.311079095
    private const val BP12 = -0.103837907
    private const val BP21 = -0.4482398e-2
    private const val BP22 = 0.6682870e-3
    private const val BP31 = -0.110248500e-2
    private const val BP32 = -0.129807200e-2
    private const val BP41 = 0.1056062e-2
    private const val BP42 = -0.1780280e-3
    private const val BP51 = 0.50928e-4
    private const val BP52 = -0.37342e-4

    // private val BP = arrayOf(BP00, BP01, BP10, BP11, BP20, BP21, BP30, BP31, BP40, BP41)

    // Constants for the evaluation of the partial derivatives.
    private const val AM = 0.074801329 // Ratio of the mean motions (EMB / Moon)
    private const val ALPHA = 0.002571881 // Ratio of the semi-major axis (Moon / EMB)
    private const val DTASM = 2.0 * ALPHA / (3.0 * AM)
    private const val XA = 2.0 * ALPHA / 3.0

    // Values of the corrections to the constants fitted to DE405 over the time interval (1950-2060).
    private const val DW1_0 = -0.07008
    private const val DW2_0 = 0.20794
    private const val DW3_0 = -0.07215
    private const val DEART_0 = -0.00033
    private const val DPERI = -0.00749
    private const val DW1_1 = -0.35106
    private const val DGAM = 0.00085
    private const val DE = -0.00006
    private const val DEART_1 = 0.00732
    private const val DEP = 0.00224
    private const val DW2_1 = 0.08017
    private const val DW3_1 = -0.04317
    private const val DW1_2 = -0.03743
    private const val DW1_3 = -0.00018865
    private const val DW1_4 = -0.00001024
    private const val DW2_2 = 0.00470602
    private const val DW2_3 = -0.00025213
    private const val DW3_2 = -0.00261070
    private const val DW3_3 = -0.00010712

    // Fundamental arguments (Moon and EMB).
    // + Corrections to the secular terms of Moon angles.
    private const val W10 = (218.0 + 18.0 / 60.0 + (59.95571 + DW1_0) / 3600.0) * DEG2RAD
    private const val W11 = (1732559343.73604 + DW1_1) * ASEC2RAD
    private const val W12 = (-6.8084 + DW1_2) * ASEC2RAD
    private const val W13 = 0.6604e-2 * ASEC2RAD + DW1_3 * ASEC2RAD
    private const val W14 = -0.3169e-4 * ASEC2RAD + DW1_4 * ASEC2RAD
    private const val W20 = (83.0 + 21.0 / 60.0 + (11.67475 + DW2_0) / 3600.0) * DEG2RAD
    private const val W21 = (14643420.3171 + DW2_1) * ASEC2RAD
    private const val W22 = -38.2631 * ASEC2RAD + DW2_2 * ASEC2RAD
    private const val W23 = -0.45047e-1 * ASEC2RAD + DW2_3 * ASEC2RAD
    private const val W24 = 0.21301e-3 * ASEC2RAD
    private const val W30 = (125.0 + 2.0 / 60.0 + (40.39816 + DW3_0) / 3600.0) * DEG2RAD
    private const val W31 = (-6967919.5383 + DW3_1) * ASEC2RAD
    private const val W32 = 6.359 * ASEC2RAD + DW3_2 * ASEC2RAD
    private const val W33 = 0.7625e-2 * ASEC2RAD + DW3_3 * ASEC2RAD
    private const val W34 = -0.3586e-4 * ASEC2RAD

    private const val EART0 = (100.0 + 27.0 / 60.0 + (59.13885 + DEART_0) / 3600.0) * DEG2RAD
    private const val EART1 = (129597742.29300 + DEART_1) * ASEC2RAD
    private const val EART2 = -0.020200 * ASEC2RAD
    private const val EART3 = 0.90000e-5 * ASEC2RAD
    private const val EART4 = 0.15000e-6 * ASEC2RAD

    private const val PERI0 = (102.0 + 56.0 / 60.0 + (14.45766 + DPERI) / 3600.0) * DEG2RAD
    private const val PERI1 = 1161.24342 * ASEC2RAD
    private const val PERI2 = 0.529265 * ASEC2RAD
    private const val PERI3 = -0.11814e-3 * ASEC2RAD
    private const val PERI4 = 0.11379e-4 * ASEC2RAD

    // Corrections to the mean motions of the Moon angles W2 and W3 -----
    // infered from the modifications of the constants
    private const val X2 = W21 / W11
    private const val X3 = W31 / W11
    private const val Y2 = AM * BP11 + XA * BP51
    private const val Y3 = AM * BP12 + XA * BP52

    private const val D21 = X2 - Y2
    private const val D22 = W11 * BP21
    private const val D23 = W11 * BP31
    private const val D24 = W11 * BP41
    private const val D25 = Y2 / AM

    private const val D31 = X3 - Y3
    private const val D32 = W11 * BP22
    private const val D33 = W11 * BP32
    private const val D34 = W11 * BP42
    private const val D35 = Y3 / AM

    private const val CW2_1 = D21 * DW1_1 + D25 * DEART_1 + D22 * DGAM + D23 * DE + D24 * DEP
    private const val CW3_1 = D31 * DW1_1 + D35 * DEART_1 + D32 * DGAM + D33 * DE + D34 * DEP
    private const val W21_2 = W21 + CW2_1 * ASEC2RAD
    private const val W31_2 = W31 + CW3_1 * ASEC2RAD

    // Arguments of Delaunay.
    private const val DEL10 = W10 - EART0 + PI
    private const val DEL20 = W10 - W30
    private const val DEL30 = W10 - W20
    private const val DEL40 = EART0 - PERI0
    private const val DEL11 = W11 - EART1
    private const val DEL21 = W11 - W31_2
    private const val DEL31 = W11 - W21_2
    private const val DEL41 = EART1 - PERI1
    private const val DEL12 = W12 - EART2
    private const val DEL22 = W12 - W32
    private const val DEL32 = W12 - W22
    private const val DEL42 = EART2 - PERI2
    private const val DEL13 = W13 - EART3
    private const val DEL23 = W13 - W33
    private const val DEL33 = W13 - W23
    private const val DEL43 = EART3 - PERI3
    private const val DEL14 = W14 - EART4
    private const val DEL24 = W14 - W34
    private const val DEL34 = W14 - W24
    private const val DEL44 = EART4 - PERI4

    // Planetary arguments (mean longitudes at J2000 and mean motions).
    private const val P10 = (252 + 15.0 / 60.0 + 3.216919 / 3600.0) * DEG2RAD
    private const val P20 = (181 + 58.0 / 60.0 + 44.758419 / 3600.0) * DEG2RAD
    private const val P30 = (100 + 27.0 / 60.0 + 59.138850 / 3600.0) * DEG2RAD
    private const val P40 = (355 + 26.0 / 60.0 + 3.642778 / 3600.0) * DEG2RAD
    private const val P50 = (34 + 21.0 / 60.0 + 5.379392 / 3600.0) * DEG2RAD
    private const val P60 = (50 + 4.0 / 60.0 + 38.902495 / 3600.0) * DEG2RAD
    private const val P70 = (314 + 3.0 / 60.0 + 4.354234 / 3600.0) * DEG2RAD
    private const val P80 = (304 + 20.0 / 60.0 + 56.808371 / 3600.0) * DEG2RAD

    private const val P11 = 538101628.66888 * ASEC2RAD
    private const val P21 = 210664136.45777 * ASEC2RAD
    private const val P31 = 129597742.29300 * ASEC2RAD
    private const val P41 = 68905077.65936 * ASEC2RAD
    private const val P51 = 10925660.57335 * ASEC2RAD
    private const val P61 = 4399609.33632 * ASEC2RAD
    private const val P71 = 1542482.57845 * ASEC2RAD
    private const val P81 = 786547.89700 * ASEC2RAD

    // Zeta: Mean longitude W1 + Rate of the precession.
    private const val ZETA0 = W10
    private const val ZETA1 = W11 + (5029.0966 + DPREC) * ASEC2RAD
    private const val ZETA2 = W12
    private const val ZETA3 = W13
    private const val ZETA4 = W14

    // Corrections factors multipled by B1-B5 for longitude and latitude.
    private const val DELNU = (0.55604 + DW1_1) * ASEC2RAD / W11
    private const val DELE = (0.01789 + DE) * ASEC2RAD
    private const val DELG = (-0.08066 + DGAM) * ASEC2RAD
    private const val DELNP = (-0.06424 + DEART_1) * ASEC2RAD / W11
    private const val DELEP = (-0.12879 + DEP) * ASEC2RAD

    // Precession coefficients for P and Q (Laskar, 1986) ---------------
    private const val P1 = 0.10180391e-04
    private const val P2 = 0.47020439e-06
    private const val P3 = -0.5417367e-09
    private const val P4 = -0.2507948e-11
    private const val P5 = 0.463486e-14
    private const val Q1 = -0.113469002e-03
    private const val Q2 = 0.12372674e-06
    private const val Q3 = 0.1265417e-08
    private const val Q4 = -0.1371808e-11
    private const val Q5 = -0.320334e-14

    private fun readMainProblemFile(type: Int): Pair<DoubleArray, Array<DoubleArray>> {
        val buffer = bufferedResource("ELP_MAIN.S$type.dat")!!

        val del = arrayOf(
            doubleArrayOf(DEL10, DEL11, DEL12, DEL13, DEL14),
            doubleArrayOf(DEL20, DEL21, DEL22, DEL23, DEL24),
            doubleArrayOf(DEL30, DEL31, DEL32, DEL33, DEL34),
            doubleArrayOf(DEL40, DEL41, DEL42, DEL43, DEL44),
        )

        return buffer.use {
            val data = ELPMPP02Reader.readMainProblemBinaryFormat(it)

            val n = data.size
            val cmpb = DoubleArray(n)
            val fmpb = Array(n) { DoubleArray(5) }
            var idx = 0

            repeat(n) {
                val elem = data[idx]
                var a = elem.a
                val tgv = elem.b0 + DTASM * elem.b4

                if (type == 3) {
                    a -= 2.0 * a * DELNU / 3.0
                }

                cmpb[idx] = a + tgv * (DELNP - AM * DELNU) + elem.b1 * DELG + elem.b2 * DELE + elem.b3 * DELEP

                for (k in 0..4) {
                    fmpb[idx][k] += elem.i0 * del[0][k]
                    fmpb[idx][k] += elem.i1 * del[1][k]
                    fmpb[idx][k] += elem.i2 * del[2][k]
                    fmpb[idx][k] += elem.i3 * del[3][k]
                }

                if (type == 3) {
                    fmpb[idx][0] += PIOVERTWO
                }

                idx++
            }

            cmpb to fmpb
        }
    }

    private fun readPertubationFile(type: Int): Array<Pair<DoubleArray, Array<DoubleArray>>> {
        val buffer = bufferedResource("ELP_PERT.S$type.dat")!!
        val res = ArrayList<Pair<DoubleArray, Array<DoubleArray>>>(4)

        val zeta = doubleArrayOf(ZETA0, ZETA1, ZETA2, ZETA3, ZETA4)

        val del = arrayOf(
            doubleArrayOf(DEL10, DEL11, DEL12, DEL13, DEL14),
            doubleArrayOf(DEL20, DEL21, DEL22, DEL23, DEL24),
            doubleArrayOf(DEL30, DEL31, DEL32, DEL33, DEL34),
            doubleArrayOf(DEL40, DEL41, DEL42, DEL43, DEL44),
        )

        val p = arrayOf(
            doubleArrayOf(P10, P11, 0.0, 0.0, 0.0),
            doubleArrayOf(P20, P21, 0.0, 0.0, 0.0),
            doubleArrayOf(P30, P31, 0.0, 0.0, 0.0),
            doubleArrayOf(P40, P41, 0.0, 0.0, 0.0),
            doubleArrayOf(P50, P51, 0.0, 0.0, 0.0),
            doubleArrayOf(P60, P61, 0.0, 0.0, 0.0),
            doubleArrayOf(P70, P71, 0.0, 0.0, 0.0),
            doubleArrayOf(P80, P81, 0.0, 0.0, 0.0),
        )

        buffer.use {
            val data = ELPMPP02Reader.readPertubationBinaryFormat(it)

            for (item in data) {
                val n = item.size
                val cper = DoubleArray(n)
                val fper = Array(n) { DoubleArray(5) }

                repeat(n) {
                    val s = item[it].s
                    val c = item[it].c
                    val i = item[it].i

                    cper[it] = sqrt(c * c + s * s)
                    fper[it][0] = atan2(c, s).normalized

                    for (k in 0..4) {
                        fper[it][k] += i[0] * del[0][k]
                        fper[it][k] += i[1] * del[1][k]
                        fper[it][k] += i[2] * del[2][k]
                        fper[it][k] += i[3] * del[3][k]

                        fper[it][k] += i[4] * p[0][k]
                        fper[it][k] += i[5] * p[1][k]
                        fper[it][k] += i[6] * p[2][k]
                        fper[it][k] += i[7] * p[3][k]
                        fper[it][k] += i[8] * p[4][k]
                        fper[it][k] += i[9] * p[5][k]
                        fper[it][k] += i[10] * p[6][k]
                        fper[it][k] += i[11] * p[7][k]

                        fper[it][k] += i[12] * zeta[k]
                    }
                }

                if (n > 0) res.add(cper to fper)
            }
        }

        return res.toTypedArray()
    }
}
