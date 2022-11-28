package nebulosa.nova.astrometry

import nebulosa.constants.DAYSPERJM
import nebulosa.constants.J2000
import nebulosa.io.bufferedResource
import nebulosa.io.readDoubleArrayLe
import nebulosa.math.Vector3D
import nebulosa.time.InstantOfTime
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Variations Séculaires des Orbites Planétaires by Bretagnon P. and Francou G.
 * ftp://ftp.imcce.fr/pub/ephem/planets/vsop87
 */
object Vsop87 : PlanetaryTheory() {

    enum class Body(override val target: Int) : nebulosa.nova.astrometry.Body {
        MERCURY(199),
        VENUS(299),
        MARS(499),
        JUPITER(599),
        SATURN(699),
        URANUS(799),
        NEPTUNE(899);

        override val center = 0

        override fun compute(time: InstantOfTime) = compute(this, time)
    }

    private const val DIM = 8 * 6
    private const val DELTA_T = 10.0 / 365250.0 // 10 days

    private var t0 = -1E+100
    private var t1 = -1E+100
    private var t2 = -1E+100

    private val elem = DoubleArray(DIM)
    private val elem0 = DoubleArray(DIM)
    private val elem1 = DoubleArray(DIM)
    private val elem2 = DoubleArray(DIM)

    private const val GAUSS_GRAV_CONST = 0.01720209895 * 0.01720209895

    private val MU = doubleArrayOf(
        (1.0 + 1.0 / 6023600) * GAUSS_GRAV_CONST,
        (1.0 + 1.0 / 408523.5) * GAUSS_GRAV_CONST,
        (1.0 + 1.0 / 328900.5) * GAUSS_GRAV_CONST,
        (1.0 + 1.0 / 3098710) * GAUSS_GRAV_CONST,
        (1.0 + 1.0 / 1047.355) * GAUSS_GRAV_CONST,
        (1.0 + 1.0 / 3498.5) * GAUSS_GRAV_CONST,
        (1.0 + 1.0 / 22869) * GAUSS_GRAV_CONST,
        (1.0 + 1.0 / 19314) * GAUSS_GRAV_CONST
    )

    private val L = doubleArrayOf(
        26087.90314157420, 10213.28554621100,
        6283.07584999140, 3340.61242669981,
        529.69096509460, 213.29909543800,
        74.78159856730, 38.13303563780,
    )

    private val MAX_LAMBDA_FACTOR = intArrayOf(16, 27, 27, 32, 22, 26, 24, 23, 2, 1, 2, 1)

    private val LAMBDA0 = doubleArrayOf(
        4.40260884240, 3.17614669689, 1.75347045953, 6.20347611291,
        0.59954649739, 0.87401675650, 5.48129387159, 5.31188628676,
        5.19846674103, 1.62790523337, 2.35555589827, 3.81034454697,
    )

    private val LAMBDA1 = doubleArrayOf(
        26087.9031415742, 10213.2855462110, 6283.0758499914, 3340.6124266998,
        529.6909650946, 213.2990954380, 74.7815985673, 38.1330356378,
        77713.7714681205, 84334.6615813083, 83286.9142695536, 83997.0911355954,
    )

    private val INDEX_TRANSLATION_TABLE = intArrayOf(
        0, 1, 2, -1, -1, -1, 3, 4,
        5, 6, -1, -1, 7, 8, 9, 10,
        11, 12, 13, 14, 15, 16, 17, 18,
        19, 20, 21, 22, 23, 24, 25, 26,
        27, 28, 29, 30, 31, 32, 33, -1,
        -1, -1, 34, 35, 36, 37, -1, -1,
        38, 39, 40, 41, 42, 43, 44, 45,
        46, 47, 48, 49, 50, 51, 52, 53,
        54, 55, 56, 57, 58, 59, 60, 61,
        62, 63, 64, -1, -1, -1, 65, 66,
        67, 68, 69, 70, 71, 72, 73, 74,
        75, 76, 77, 78, 79, 80, 81, 82,
        83, 84, 85, 86, 87, 88, 89, 90,
        91, 92, 93, 94, 95, 96, 97, -1,
        -1, -1, 98, 99, 100, 101, 102, 103,
        104, 105, 106, 107, 108, 109, 110, 111,
        112, 113, 114, 115, 116, 117, 118, 119,
        120, 121, 122, 123, 124, 125, 126, -1,
        127, 128, 129, 130, 131, 132, 133, 134,
        135, 136, 137, 138, 139, 140, 141, 142,
        143, -1, 144, 145, 146, 147, 148, -1,
        149, 150, 151, 152, -1, -1, 153, 154,
        155, 156, -1, -1, 157, 158, 159, 160,
        161, 162, 163, 164, 165, 166, 167, 168,
        169, 170, 171, 172, 173, 174, 175, 176,
        177, 178, 179, 180, 181, 182, 183, 184,
        185, -1, 186, 187, 188, 189, 190, -1,
        191, 192, 193, 194, 195, 196, 197, 198,
        199, 200, 201, 202, 203, 204, 205, 206,
        207, -1, 208, 209, 210, 211, 212, -1,
        213, 214, 215, 216, -1, -1, 217, 218,
        219, 220, -1, -1, 221, 222, 223, 224,
        225, 226, 227, 228, 229, 230, 231, 232,
        233, 234, 235, 236, 237, 238, 239, 240,
        241, 242, 243, 244, 245, 246, 247, 248,
        -1, -1, 249, 250, 251, 252, -1, -1,
    )

    private val INSTRUCTIONS = bufferedResource("VSOP87_INSTRUCTIONS.dat") { readByteArray() }
    private val CONSTANTS = bufferedResource("VSOP87_CONSTANTS.dat") { readDoubleArrayLe(253) }
    private val COEFFICIENTS = bufferedResource("VSOP87_COEFFICIENTS.dat") { readDoubleArrayLe(121828) }

    fun compute(body: Body, time: InstantOfTime): Pair<Vector3D, Vector3D> {
        val t = (time.tdb.whole - J2000 + time.tdb.fraction) / DAYSPERJM

        val ts = doubleArrayOf(t0, t1, t2)
        val es = arrayOf(elem0, elem1, elem2)

        computeInterpolatedElements(t, elem, DIM, ::computeVsop87Elem, DELTA_T, ts, es)

        t0 = ts[0]
        t1 = ts[1]
        t2 = ts[2]

        val be = elem.sliceArray(body.ordinal * 6 until body.ordinal * 6 + 6)

        return ellipticToRectangularA(MU[body.ordinal], be, 0.0)
    }

    private fun computeVsop87Elem(t: Double, elem: DoubleArray) {
        val lambda = DoubleArray(12)
        val cosSinLambda = DoubleArray(203 * 4)
        val accu = DoubleArray(CONSTANTS.size)
        val stack = DoubleArray(12 * 2)

        for (i in 0..11) lambda[i] = LAMBDA0[i] + LAMBDA1[i] * t

        prepareLambdaArray(12, MAX_LAMBDA_FACTOR, lambda, cosSinLambda)

        accumulateVsop87Terms(INSTRUCTIONS, COEFFICIENTS, cosSinLambda, accu, stack)

        elem.fill(0.0)

        // Terms of order t ^ alpha
        val usePolynomials = min(1.0, (6.1 - abs(t)) / 0.1)

        if (usePolynomials > 0) {
            for (i in elem.indices) {
                for (alpha in 5 downTo 1) {
                    val j = INDEX_TRANSLATION_TABLE[i * 6 + alpha]

                    if (j >= 0) {
                        elem[i] += accu[j] + CONSTANTS[j]
                        elem[i] *= t
                    }
                }

                elem[i] *= usePolynomials
            }
        }

        // Terms of order t^0
        for (i in elem.indices) {
            val j = INDEX_TRANSLATION_TABLE[i * 6]
            elem[i] += accu[j] + CONSTANTS[j]
        }

        // Longitudes
        for (i in 0..7) {
            elem[i * 6 + 1] += t * L[i]
        }
    }

    private fun prepareLambdaArray(
        n: Int,
        maxLambdaFactor: IntArray,
        lambda: DoubleArray,
        cosSinLambda: DoubleArray,
    ) {
        var csli = 0

        for (i in 0 until n) {
            var cslp = csli
            val maxFactor = maxLambdaFactor[i]

            cosSinLambda[0 + cslp] = cos(lambda[i])
            cosSinLambda[1 + cslp] = sin(lambda[i])
            cosSinLambda[2 + cslp] = cosSinLambda[0 + cslp]
            cosSinLambda[3 + cslp] = -cosSinLambda[1 + cslp]

            for (m in 2..maxFactor) {
                val m0 = csli + ((((m + 0) shr 1) - 1) shl 2)
                val m1 = csli + ((((m + 1) shr 1) - 1) shl 2)

                cslp += 4

                cosSinLambda[0 + cslp] =
                    cosSinLambda[m0] * cosSinLambda[m1] - cosSinLambda[m0 + 1] * cosSinLambda[m1 + 1]
                cosSinLambda[1 + cslp] =
                    cosSinLambda[m0] * cosSinLambda[m1 + 1] + cosSinLambda[m0 + 1] * cosSinLambda[m1]
                cosSinLambda[2 + cslp] = cosSinLambda[0 + cslp]
                cosSinLambda[3 + cslp] = -cosSinLambda[1 + cslp]
            }

            csli += (maxFactor shl 2)
        }
    }

    private fun accumulateVsop87Terms(
        instructions: ByteArray,
        coefficients: DoubleArray,
        cosSinLambda: DoubleArray,
        accu: DoubleArray,
        sp: DoubleArray,
    ) {
        var spi = 0

        sp[0 + spi] = 1.0
        sp[1 + spi] = 0.0

        var i = 0
        var ci = 0

        while (true) {
            var lambdaIndex = instructions[i++].toInt() and 0xFF

            when (lambdaIndex) {
                0xFF -> break
                0xFE -> spi -= 2 // Pop argument from the stack
                else -> {
                    lambdaIndex = (lambdaIndex shl 8) or (instructions[i++].toInt() and 0xFF)
                    var termCount = instructions[i++].toInt() and 0xFF
                    val cosSin = lambdaIndex shl 1

                    sp[2 + spi] = cosSinLambda[0 + cosSin] * sp[0 + spi] - cosSinLambda[1 + cosSin] * sp[1 + spi]
                    sp[3 + spi] = cosSinLambda[0 + cosSin] * sp[1 + spi] + cosSinLambda[1 + cosSin] * sp[0 + spi]

                    spi += 2

                    while (--termCount >= 0) {
                        accu[instructions[i++].toInt() and 0xFF] += coefficients[0 + ci] * sp[0 + spi] + coefficients[1 + ci] * sp[1 + spi]
                        ci += 2
                    }
                }
            }
        }
    }
}
