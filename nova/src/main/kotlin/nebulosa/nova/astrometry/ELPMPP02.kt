package nebulosa.nova.astrometry

import nebulosa.constants.*
import nebulosa.erfa.eraAnpm
import nebulosa.io.bufferedResource
import nebulosa.math.Angle
import nebulosa.math.Angle.Companion.arcsec
import nebulosa.math.Angle.Companion.deg
import nebulosa.math.Angle.Companion.rad
import nebulosa.math.Matrix3D
import nebulosa.math.Vector3D
import nebulosa.time.InstantOfTime
import java.util.*
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Accurate Moon positions using the Lunar solution ELP/MPP02.
 */
object ELPMPP02 : Body {

    private val mainDist = readMainProblemFile("DIST")
    private val mainLat = readMainProblemFile("LAT")
    private val mainLong = readMainProblemFile("LONG")

    private val pertDistT0 = readPertubationFile("DIST", 0)
    private val pertLatT0 = readPertubationFile("LAT", 0)
    private val pertLongT0 = readPertubationFile("LONG", 0)

    private val pertDistT1 = readPertubationFile("DIST", 1)
    private val pertLatT1 = readPertubationFile("LAT", 1)
    private val pertLongT1 = readPertubationFile("LONG", 1)

    private val pertDistT2 = readPertubationFile("DIST", 2)
    private val pertLatT2 = readPertubationFile("LAT", 2)
    private val pertLongT2 = readPertubationFile("LONG", 2)

    private val pertDistT3 = readPertubationFile("DIST", 3)
    private val pertLongT3 = readPertubationFile("LONG", 3)

    override val center = 399 // Earth.

    override val target = 301 // Moon.

    override fun compute(time: InstantOfTime): Pair<Vector3D, Vector3D> {
        val t0 = (time.tdb.whole - J2000 + time.tdb.fraction) / DAYSPERJC
        val args = computeELPArguments(t0)

        val mainLong = computeMainSum(mainLong, args, 0)
        val mainLat = computeMainSum(mainLat, args, 0)
        val mainDist = computeMainSum(mainDist, args, 1)
        val pertLongT0 = computePertSum(pertLongT0, args)
        val pertLongT1 = computePertSum(pertLongT1, args)
        val pertLongT2 = computePertSum(pertLongT2, args)
        val pertLongT3 = computePertSum(pertLongT3, args)
        val pertLatT0 = computePertSum(pertLatT0, args)
        val pertLatT1 = computePertSum(pertLatT1, args)
        val pertLatT2 = computePertSum(pertLatT2, args)
        val pertDistT0 = computePertSum(pertDistT0, args)
        val pertDistT1 = computePertSum(pertDistT1, args)
        val pertDistT2 = computePertSum(pertDistT2, args)
        val pertDistT3 = computePertSum(pertDistT3, args)

        // Moon's longitude, latitude and distance.
        val longM = args.w1 + mainLong.rad + pertLongT0.rad + eraAnpm(pertLongT1.rad * args.t[0]) +
                eraAnpm(pertLongT2.rad * args.t[1]) + eraAnpm(pertLongT3.rad * args.t[2])
        val latM = mainLat.rad + pertLatT0.rad + eraAnpm(pertLatT1.rad * args.t[0]) + eraAnpm(pertLatT2.rad * args.t[1])
        val r = (mainDist + pertDistT0 + pertDistT1 * args.t[0] + pertDistT2 * args.t[1] + pertDistT3 * args.t[2]) * RA0

        val clamb = longM.cos
        val slamb = longM.sin
        val cbeta = latM.cos
        val sbeta = latM.sin
        val cw = r * cbeta
        val sw = r * sbeta
        val x0 = cw * clamb
        val y0 = cw * slamb
        val z0 = sw
        val xp0 = 0.0
        val yp0 = 0.0
        val zp0 = 0.0

        // Precession matrix.
        val pw =
            0.10180391e-4 * args.t[0] + 0.47020439e-6 * args.t[1] - 0.5417367e-9 * args.t[2] - 0.2507948e-11 * args.t[3] + 0.463486e-14 * args.t[4]
        val qw =
            -0.113469002e-3 * args.t[0] + 0.12372674e-6 * args.t[1] + 0.12654170e-8 * args.t[2] - 0.1371808e-11 * args.t[3] - 0.320334e-14 * args.t[4]
        val sq = sqrt(1.0 - pw * pw - qw * qw)
        val ra = 2.0 * sq
        val pw2 = 1.0 - 2.0 * pw * pw
        val pwqw = 2.0 * pw * qw
        val pwra = pw * ra
        val qw2 = 1.0 - 2.0 * qw * qw
        val qwra = qw * ra

        // Finally, components of position vector wrt J2000.0 mean ecliptic and equinox.
        val x = (pw2 * x0 + pwqw * y0 + pwra * z0) / AU_KM
        val y = (pwqw * x0 + qw2 * y0 - qwra * z0) / AU_KM
        val z = (-pwra * x0 + qwra * y0 + (pw2 + qw2 - 1.0) * z0) / AU_KM

        val ppw =
            0.10180391e-4 + (2.0 * 0.47020439e-6 + 3.0 * 0.5417367e-9 * args.t[0] + 4.0 * 0.2507948e-11 * args.t[1] + 5.0 * 0.463486e-14 * args.t[2]) * args.t[0]
        val qpw =
            -0.113469002e-3 + (2.0 * 0.12372674e-6 + 3.0 * 0.12654170e-8 * args.t[0] + 4.0 * 0.1371808e-11 * args.t[1] + 5.0 * 0.320334e-14 * args.t[3]) * args.t[0]
        val ppw2 = -4 * pw * ppw
        val qpw2 = -4 * qw * qpw
        val ppwqpw = 2 * (ppw * qw + pw * qpw)
        val rap = (ppw2 + qpw2) / ra
        val ppwra = ppw * ra + pw * rap
        val qpwra = qpw * ra + qw * rap

//        val vx = (pw2 * xp1 + pwqw * xp2 + pwra * xp3 + ppw2 * x0 + ppwqpw * y0 + ppwra * z0) / DAYSPERJC
//        val vy = (pwqw * xp1 + qw2 * xp2 - qwra * xp3 + ppwqpw * x0 + qpw2 * y0 - qpwra * z0) / DAYSPERJC
//        val vz = (-pwra * xp1 + qwra * xp2 + (pw2 + qw2 - 1) * xp3 - ppwra * x0 + qpwra * y0 + (ppw2 + qpw2) * z0) / DAYSPERJC

        return REFERENCE_FRAME * Vector3D(x, y, z) to REFERENCE_FRAME * Vector3D.EMPTY
    }

    @JvmStatic private val REFERENCE_FRAME = Matrix3D(
        1.000000000000, 0.000000440360, -0.000000190919,
        -0.000000479966, 0.917482137087, -0.397776982902,
        0.000000000000, 0.397776982902, 0.917482137087,
    )

    private const val RA0 = 384747.961370173 / 384747.980674318

    // Constant for the correction to the constant of precession - source: IAU 2000A.
    private const val PREC = -0.29965

    private const val BP00 = 0.311079095
    private const val BP01 = -0.4482398e-2
    private const val BP10 = -0.110248500e-2
    private const val BP11 = 0.1056062e-2
    private const val BP20 = 0.50928e-4
    private const val BP21 = -0.103837907
    private const val BP30 = 0.6682870e-3
    private const val BP31 = -0.129807200e-2
    private const val BP40 = -0.1780280e-3
    private const val BP41 = -0.37342e-4

    // @JvmStatic private val BP = arrayOf(BP00, BP01, BP10, BP11, BP20, BP21, BP30, BP31, BP40, BP41)

    private const val AM = 0.074801329 // Ratio of the mean motions (EMB / Moon)
    private const val ALPHA = 0.002571881 // Ratio of the semi-major axis (Moon / EMB)
    private const val DTSM = (2.0 * ALPHA) / (3.0 * AM)
    private const val XA = (2.0 * ALPHA) / 3.0

    // Corrections to constants: DE405.
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

    private const val W11 = (1732559343.73604 + DW1_1) * ASEC2RAD
    private const val W21 = (14643420.3171 + DW2_1) * ASEC2RAD
    private const val W31 = (-6967919.5383 + DW3_1) * ASEC2RAD
    private const val X2 = W21 / W11
    private const val X3 = W31 / W11
    private const val Y2 = AM * BP00 + XA * BP40
    private const val Y3 = AM * BP01 + XA * BP41
    private const val D21 = X2 - Y2
    private const val D22 = W11 * BP10
    private const val D23 = W11 * BP20
    private const val D24 = W11 * BP30
    private const val D25 = Y2 / AM
    private const val D31 = X3 - Y3
    private const val D32 = W11 * BP11
    private const val D33 = W11 * BP21
    private const val D34 = W11 * BP31
    private const val D35 = Y3 / AM
    private const val CW2_1 = D21 * DW1_1 + D25 * DEART_1 + D22 * DGAM + D23 * DE + D24 * DEP
    private const val CW3_1 = D31 * DW1_1 + D35 * DEART_1 + D32 * DGAM + D33 * DE + D34 * DEP

    // Factors multipled by B1-B5 for longitude and latitude.
    private const val DELNU_NU = (0.55604 + DW1_1) * ASEC2RAD / W11
    private const val DELE = (0.01789 + DE) * ASEC2RAD
    private const val DELG = (-0.08066 + DGAM) * ASEC2RAD
    private const val DELNP_NU = (-0.06424 + DEART_1) * ASEC2RAD / W11
    private const val DELEP = (-0.12879 + DEP) * ASEC2RAD

    // Factors multipled by B1-B5 for longitude and latitude.
    private const val FB1 = -AM * DELNU_NU + DELNP_NU
    private const val FB2 = DELG
    private const val FB3 = DELE
    private const val FB4 = DELEP
    private const val FB5 = -XA * DELNU_NU + DTSM * DELNP_NU

    // Factor multiplie A_i for distance.
    private const val FA = 1.0 - 2.0 / 3.0 * DELNU_NU

    @Suppress("ArrayInDataClass")
    private data class ElpArguments(
        @Transient val t: DoubleArray,
        val w1: Angle,
        val d: Angle,
        val f: Angle,
        val l: Angle,
        val lp: Angle,
        val zeta: Angle,
        val me: Angle,
        val ve: Angle,
        val em: Angle,
        val ma: Angle,
        val ju: Angle,
        val sa: Angle,
        val ur: Angle,
        val ne: Angle,
    )

    private fun computeELPArguments(t0: Double): ElpArguments {
        val t = DoubleArray(5)
        t[0] = t0
        for (i in 1..4) t[i] = t[i - 1] * t0

        val w10 = (-142.0 + 18.0 / 60.0 + (59.95571 + DW1_0) / 3600.0).deg
        val w11 = eraAnpm(((1732559343.73604 + DW1_1) * t[0]).arcsec)
        val w12 = eraAnpm(((-6.8084 + DW1_2) * t[1]).arcsec)
        val w13 = eraAnpm(((0.006604 + DW1_3) * t[2]).arcsec)
        val w14 = eraAnpm(((-3.169e-5 + DW1_4) * t[3]).arcsec)
        val w20 = ((83.0 + 21.0 / 60.0 + (11.67475 + DW2_0) / 3600.0)).deg
        val w21 = eraAnpm(((14643420.3171 + DW2_1 + CW2_1) * t[0]).arcsec)
        val w22 = eraAnpm(((-38.2631 + DW2_2) * t[1]).arcsec)
        val w23 = eraAnpm(((-0.045047 + DW2_3) * t[2]).arcsec)
        val w24 = eraAnpm((0.00021301 * t[3]).arcsec)
        val w30 = (125.0 + 2.0 / 60.0 + (40.39816 + DW3_0) / 3600.0).deg
        val w31 = eraAnpm(((-6967919.5383 + DW3_1 + CW3_1) * t[0]).arcsec)
        val w32 = eraAnpm(((6.359 + DW3_2) * t[1]).arcsec)
        val w33 = eraAnpm(((0.007625 + DW3_3) * t[2]).arcsec)
        val w34 = eraAnpm((-3.586e-5 * t[3]).arcsec)
        val ea0 = (100.0 + 27.0 / 60.0 + (59.13885 + DEART_0) / 3600.0).deg
        val ea1 = eraAnpm(((129597742.293 + DEART_1) * t[0]).arcsec)
        val ea2 = eraAnpm((-0.0202 * t[1]).arcsec)
        val ea3 = eraAnpm((9e-6 * t[2]).arcsec)
        val ea4 = eraAnpm((1.5e-7 * t[3]).arcsec)
        val p0 = (102.0 + 56.0 / 60.0 + (14.45766 + DPERI) / 3600.0).deg
        val p1 = eraAnpm((1161.24342 * t[0]).arcsec)
        val p2 = eraAnpm((0.529265 * t[1]).arcsec)
        val p3 = eraAnpm((-1.1814e-4 * t[2]).arcsec)
        val p4 = eraAnpm((1.1379e-5 * t[3]).arcsec)

        var me = (-108.0 + 15.0 / 60.0 + 3.216919 / 3600.0).deg
        me += eraAnpm((538101628.66888 * t[0]).arcsec)
        var ve = (-179.0 + 58.0 / 60.0 + 44.758419 / 3600.0).deg
        ve += eraAnpm((210664136.45777 * t[0]).arcsec)
        var em = (100.0 + 27.0 / 60.0 + 59.13885 / 3600.0).deg
        em += eraAnpm((129597742.293 * t[0]).arcsec)
        var ma = (-5.0 + 26.0 / 60.0 + 3.642778 / 3600.0).deg
        ma += eraAnpm((68905077.65936 * t[0]).arcsec)
        var ju = (34.0 + 21.0 / 60.0 + 5.379392 / 3600.0).deg
        ju += eraAnpm((10925660.57335 * t[0]).arcsec)
        var sa = (50.0 + 4.0 / 60.0 + 38.902495 / 3600.0).deg
        sa += eraAnpm((4399609.33632 * t[0]).arcsec)
        var ur = (-46.0 + 3.0 / 60.0 + 4.354234 / 3600.0).deg
        ur += eraAnpm((1542482.57845 * t[0]).arcsec)
        var ne = (-56.0 + 20.0 / 60.0 + 56.808371 / 3600.0).deg
        ne += eraAnpm((786547.897 * t[0]).arcsec)

        // Mean longitude of the Moon.
        val w1 = eraAnpm(w10 + w11 + w12 + w13 + w14)

        val w2 = w20 + w21 + w22 + w23 + w24
        val w3 = w30 + w31 + w32 + w33 + w34
        val ea = ea0 + ea1 + ea2 + ea3 + ea4
        val pomp = p0 + p1 + p2 + p3 + p4

        // Arguments of Delaunay.
        val d = eraAnpm(w1 - ea + PI)
        val f = eraAnpm(w1 - w3)
        val l = eraAnpm(w1 - w2)
        val lp = eraAnpm(ea - pomp)

        // zeta
        val zeta = eraAnpm(w1 + 0.02438029560881907 * t[0])

        // Planetary arguments (mean longitudes and mean motions)
        me = eraAnpm(me)
        ve = eraAnpm(ve)
        em = eraAnpm(em)
        ma = eraAnpm(ma)
        ju = eraAnpm(ju)
        sa = eraAnpm(sa)
        ur = eraAnpm(ur)
        ne = eraAnpm(ne)

        return ElpArguments(t, w1, d, f, l, lp, zeta, me, ve, em, ma, ju, sa, ur, ne)
    }

    private fun computeMainSum(
        main: Pair<Array<IntArray>, DoubleArray>,
        args: ElpArguments,
        dist: Int,
    ): Double {
        val (i, a) = main

        var sum = 0.0

        if (dist == 0) {
            // Sine.
            for (k in a.indices) {
                val phase = i[k][0] * args.d.value + i[k][1] * args.f.value +
                        i[k][2] * args.l.value + i[k][3] * args.lp.value
                sum += a[k] * sin(phase)
            }
        } else {
            // Cosine.
            for (k in a.indices) {
                val phase = i[k][0] * args.d.value + i[k][1] * args.f.value +
                        i[k][2] * args.l.value + i[k][3] * args.lp.value
                sum += a[k] * cos(phase)
            }
        }

        return sum
    }

    private fun computePertSum(
        pert: Triple<Array<IntArray>, DoubleArray, DoubleArray>,
        args: ElpArguments,
    ): Double {
        val (i, a, ph) = pert

        var sum = 0.0

        for (k in a.indices) {
            val phase = ph[k] + i[k][0] * args.d.value + i[k][1] * args.f.value +
                    i[k][2] * args.l.value + i[k][3] * args.lp.value + i[k][4] * args.me.value +
                    i[k][5] * args.ve.value + i[k][6] * args.em.value + i[k][7] * args.ma.value +
                    i[k][8] * args.ju.value + i[k][9] * args.sa.value + i[k][10] * args.ur.value +
                    i[k][11] * args.ne.value + i[k][12] * args.zeta.value
            sum += a[k] * sin(phase)
        }

        return sum
    }

    @Suppress("LocalVariableName")
    private fun readMainProblemFile(type: String): Pair<Array<IntArray>, DoubleArray> {
        val buffer = bufferedResource("ELP_MAIN_$type.txt")!!

        var firstLine = true
        var n: Int
        var idx = 0

        var i = Array(0) { IntArray(0) }
        var a = DoubleArray(0)

        while (!buffer.exhausted()) {
            val line = buffer.readUtf8Line() ?: break

            if (firstLine) {
                firstLine = false
                n = line.trim().toInt()
                i = Array(n) { IntArray(4) }
                a = DoubleArray(n)
            } else {
                val reader = Scanner(line)
                reader.useLocale(Locale.ENGLISH)

                for (k in 0..3) i[idx][k] = reader.nextInt()

                val A = reader.nextDouble()
                val B1 = reader.nextDouble()
                val B2 = reader.nextDouble()
                val B3 = reader.nextDouble()
                val B4 = reader.nextDouble()
                val B5 = reader.nextDouble()
                // val B6 = reader.nextDouble()
                a[idx++] = FA * A + FB1 * B1 + FB2 * B2 + FB3 * B3 + FB4 * B4 + FB5 * B5
            }
        }

        buffer.close()

        return i to a
    }

    private fun readPertubationFile(type: String, exp: Int): Triple<Array<IntArray>, DoubleArray, DoubleArray> {
        val buffer = bufferedResource("ELP_PERT_${type}_T$exp.txt")!!

        var firstLine = true
        var n: Int
        var idx = 0

        var i = Array(0) { IntArray(0) }
        var a = DoubleArray(0)
        var phase = DoubleArray(0)

        while (!buffer.exhausted()) {
            val line = buffer.readUtf8Line() ?: break

            if (firstLine) {
                firstLine = false
                n = line.trim().toInt()
                i = Array(n) { IntArray(13) }
                a = DoubleArray(n)
                phase = DoubleArray(n)
            } else {
                val reader = Scanner(line)
                reader.useLocale(Locale.ENGLISH)

                for (k in 0..12) i[idx][k] = reader.nextInt()
                a[idx] = reader.nextDouble()
                phase[idx++] = reader.nextDouble()
            }
        }

        buffer.close()

        return Triple(i, a, phase)
    }
}
