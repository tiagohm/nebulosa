package nebulosa.nova.astrometry

import nebulosa.constants.AU_KM
import nebulosa.constants.DAYSPERJM
import nebulosa.constants.J2000
import nebulosa.io.bufferedResource
import nebulosa.io.readDoubleArrayLe
import nebulosa.math.Vector3D
import nebulosa.time.InstantOfTime
import kotlin.math.cos
import kotlin.math.sin

/**
 * Variations Séculaires des Orbites Planétaires by Bretagnon P. and Francou G.
 * @see <a href="https://ftp.imcce.fr/pub/ephem/planets/vsop87/">Serveur FTP public de lʼIMCCE</a>
 * @see <a href="https://github.com/gmiller123456/vsop87-multilang/blob/master/Languages/Java">VSOP87 in multiple programming languages</a>
 */
sealed class VSOP87A(
    override val target: Int,
    name: String,
    xSizes: IntArray,
    ySizes: IntArray,
    zSizes: IntArray,
) : Body {

    private val x: Array<DoubleArray>
    private val y: Array<DoubleArray>
    private val z: Array<DoubleArray>
    private val xyz: Array<Array<DoubleArray>>

    init {
        val buffer = bufferedResource(name)!!
        x = Array(xSizes.size) { buffer.readDoubleArrayLe(xSizes[it]) }
        y = Array(ySizes.size) { buffer.readDoubleArrayLe(ySizes[it]) }
        z = Array(zSizes.size) { buffer.readDoubleArrayLe(zSizes[it]) }
        xyz = arrayOf(x, y, z)
        buffer.close()
    }

    override val center = 10 // Heliocentric.

    override fun compute(time: InstantOfTime): Pair<Vector3D, Vector3D> {
        val t = DoubleArray(6)
        t[0] = 1.0
        t[1] = (time.tdb.whole - J2000 + time.tdb.fraction) / DAYSPERJM
        for (i in 2..5) t[i] = t[i - 1] * t[1]

        val pos = DoubleArray(3)
        val vel = DoubleArray(3)

        for (i in 0..2) {
            for (k in xyz[i].indices) {
                val (p, v) = computeSeries(xyz[i][k], t[1])
                pos[i] += p * t[k]
                vel[i] += v * t[k] + if (k > 0) k * t[k - 1] * p else 0.0
            }

            pos[i] *= AU_KM
            vel[i] *= AU_KM / DAYSPERJM
        }

        return Vector3D(pos) to Vector3D(vel)
    }

    object Mercury : VSOP87A(
        199, "VSOP87A_MERCURY.dat",
        intArrayOf(MERCURY_X_0_SIZE, MERCURY_X_1_SIZE, MERCURY_X_2_SIZE, MERCURY_X_3_SIZE, MERCURY_X_4_SIZE, MERCURY_X_5_SIZE),
        intArrayOf(MERCURY_Y_0_SIZE, MERCURY_Y_1_SIZE, MERCURY_Y_2_SIZE, MERCURY_Y_3_SIZE, MERCURY_Y_4_SIZE, MERCURY_Y_5_SIZE),
        intArrayOf(MERCURY_Z_0_SIZE, MERCURY_Z_1_SIZE, MERCURY_Z_2_SIZE, MERCURY_Z_3_SIZE, MERCURY_Z_4_SIZE, MERCURY_Z_5_SIZE),
    )

    object Venus : VSOP87A(
        299, "VSOP87A_VENUS.dat",
        intArrayOf(VENUS_X_0_SIZE, VENUS_X_1_SIZE, VENUS_X_2_SIZE, VENUS_X_3_SIZE, VENUS_X_4_SIZE, VENUS_X_5_SIZE),
        intArrayOf(VENUS_Y_0_SIZE, VENUS_Y_1_SIZE, VENUS_Y_2_SIZE, VENUS_Y_3_SIZE, VENUS_Y_4_SIZE, VENUS_Y_5_SIZE),
        intArrayOf(VENUS_Z_0_SIZE, VENUS_Z_1_SIZE, VENUS_Z_2_SIZE, VENUS_Z_3_SIZE, VENUS_Z_4_SIZE, VENUS_Z_5_SIZE),
    )

    object Earth : VSOP87A(
        399, "VSOP87A_EARTH.dat",
        intArrayOf(EARTH_X_0_SIZE, EARTH_X_1_SIZE, EARTH_X_2_SIZE, EARTH_X_3_SIZE, EARTH_X_4_SIZE, EARTH_X_5_SIZE),
        intArrayOf(EARTH_Y_0_SIZE, EARTH_Y_1_SIZE, EARTH_Y_2_SIZE, EARTH_Y_3_SIZE, EARTH_Y_4_SIZE, EARTH_Y_5_SIZE),
        intArrayOf(EARTH_Z_0_SIZE, EARTH_Z_1_SIZE, EARTH_Z_2_SIZE, EARTH_Z_3_SIZE, EARTH_Z_4_SIZE, EARTH_Z_5_SIZE),
    )

    object Mars : VSOP87A(
        499, "VSOP87A_MARS.dat",
        intArrayOf(MARS_X_0_SIZE, MARS_X_1_SIZE, MARS_X_2_SIZE, MARS_X_3_SIZE, MARS_X_4_SIZE, MARS_X_5_SIZE),
        intArrayOf(MARS_Y_0_SIZE, MARS_Y_1_SIZE, MARS_Y_2_SIZE, MARS_Y_3_SIZE, MARS_Y_4_SIZE, MARS_Y_5_SIZE),
        intArrayOf(MARS_Z_0_SIZE, MARS_Z_1_SIZE, MARS_Z_2_SIZE, MARS_Z_3_SIZE, MARS_Z_4_SIZE, MARS_Z_5_SIZE),
    )

    object Jupiter : VSOP87A(
        599, "VSOP87A_JUPITER.dat",
        intArrayOf(JUPITER_X_0_SIZE, JUPITER_X_1_SIZE, JUPITER_X_2_SIZE, JUPITER_X_3_SIZE, JUPITER_X_4_SIZE, JUPITER_X_5_SIZE),
        intArrayOf(JUPITER_Y_0_SIZE, JUPITER_Y_1_SIZE, JUPITER_Y_2_SIZE, JUPITER_Y_3_SIZE, JUPITER_Y_4_SIZE, JUPITER_Y_5_SIZE),
        intArrayOf(JUPITER_Z_0_SIZE, JUPITER_Z_1_SIZE, JUPITER_Z_2_SIZE, JUPITER_Z_3_SIZE, JUPITER_Z_4_SIZE, JUPITER_Z_5_SIZE),
    )

    object Saturn : VSOP87A(
        699, "VSOP87A_SATURN.dat",
        intArrayOf(SATURN_X_0_SIZE, SATURN_X_1_SIZE, SATURN_X_2_SIZE, SATURN_X_3_SIZE, SATURN_X_4_SIZE, SATURN_X_5_SIZE),
        intArrayOf(SATURN_Y_0_SIZE, SATURN_Y_1_SIZE, SATURN_Y_2_SIZE, SATURN_Y_3_SIZE, SATURN_Y_4_SIZE, SATURN_Y_5_SIZE),
        intArrayOf(SATURN_Z_0_SIZE, SATURN_Z_1_SIZE, SATURN_Z_2_SIZE, SATURN_Z_3_SIZE, SATURN_Z_4_SIZE, SATURN_Z_5_SIZE),
    )

    object Uranus : VSOP87A(
        799, "VSOP87A_URANUS.dat",
        intArrayOf(URANUS_X_0_SIZE, URANUS_X_1_SIZE, URANUS_X_2_SIZE, URANUS_X_3_SIZE, URANUS_X_4_SIZE),
        intArrayOf(URANUS_Y_0_SIZE, URANUS_Y_1_SIZE, URANUS_Y_2_SIZE, URANUS_Y_3_SIZE, URANUS_Y_4_SIZE),
        intArrayOf(URANUS_Z_0_SIZE, URANUS_Z_1_SIZE, URANUS_Z_2_SIZE, URANUS_Z_3_SIZE),
    )

    object Neptune : VSOP87A(
        899, "VSOP87A_NEPTUNE.dat",
        intArrayOf(NEPTUNE_X_0_SIZE, NEPTUNE_X_1_SIZE, NEPTUNE_X_2_SIZE, NEPTUNE_X_3_SIZE, NEPTUNE_X_4_SIZE),
        intArrayOf(NEPTUNE_Y_0_SIZE, NEPTUNE_Y_1_SIZE, NEPTUNE_Y_2_SIZE, NEPTUNE_Y_3_SIZE, NEPTUNE_Y_4_SIZE),
        intArrayOf(NEPTUNE_Z_0_SIZE, NEPTUNE_Z_1_SIZE, NEPTUNE_Z_2_SIZE, NEPTUNE_Z_3_SIZE),
    )

    object EarthMoonBarycenter : VSOP87A(
        3, "VSOP87A_EMB.dat",
        intArrayOf(EMB_X_0_SIZE, EMB_X_1_SIZE, EMB_X_2_SIZE, EMB_X_3_SIZE, EMB_X_4_SIZE, EMB_X_5_SIZE),
        intArrayOf(EMB_Y_0_SIZE, EMB_Y_1_SIZE, EMB_Y_2_SIZE, EMB_Y_3_SIZE, EMB_Y_4_SIZE, EMB_Y_5_SIZE),
        intArrayOf(EMB_Z_0_SIZE, EMB_Z_1_SIZE, EMB_Z_2_SIZE, EMB_Z_3_SIZE, EMB_Z_4_SIZE, EMB_Z_5_SIZE),
    )

    object Moon : Body {

        private val moon = EarthMoonBarycenter - Earth

        override val center = 399

        override val target = 301

        override fun compute(time: InstantOfTime): Pair<Vector3D, Vector3D> {
            val (p, v) = moon.compute(time)
            return (p * (1.0 + 1.0 / 0.01230073677)) to v
        }
    }

    companion object {

        private const val EARTH_X_0_SIZE = 2529
        private const val EARTH_X_1_SIZE = 1473
        private const val EARTH_X_2_SIZE = 612
        private const val EARTH_X_3_SIZE = 54
        private const val EARTH_X_4_SIZE = 45
        private const val EARTH_X_5_SIZE = 18
        private const val EARTH_Y_0_SIZE = 2562
        private const val EARTH_Y_1_SIZE = 1488
        private const val EARTH_Y_2_SIZE = 606
        private const val EARTH_Y_3_SIZE = 51
        private const val EARTH_Y_4_SIZE = 45
        private const val EARTH_Y_5_SIZE = 18
        private const val EARTH_Z_0_SIZE = 534
        private const val EARTH_Z_1_SIZE = 360
        private const val EARTH_Z_2_SIZE = 159
        private const val EARTH_Z_3_SIZE = 36
        private const val EARTH_Z_4_SIZE = 18
        private const val EARTH_Z_5_SIZE = 6
        private const val VENUS_X_0_SIZE = 1644
        private const val VENUS_X_1_SIZE = 1014
        private const val VENUS_X_2_SIZE = 297
        private const val VENUS_X_3_SIZE = 15
        private const val VENUS_X_4_SIZE = 12
        private const val VENUS_X_5_SIZE = 9
        private const val VENUS_Y_0_SIZE = 1695
        private const val VENUS_Y_1_SIZE = 975
        private const val VENUS_Y_2_SIZE = 297
        private const val VENUS_Y_3_SIZE = 15
        private const val VENUS_Y_4_SIZE = 12
        private const val VENUS_Y_5_SIZE = 9
        private const val VENUS_Z_0_SIZE = 570
        private const val VENUS_Z_1_SIZE = 324
        private const val VENUS_Z_2_SIZE = 135
        private const val VENUS_Z_3_SIZE = 30
        private const val VENUS_Z_4_SIZE = 9
        private const val VENUS_Z_5_SIZE = 9
        private const val MERCURY_X_0_SIZE = 4347
        private const val MERCURY_X_1_SIZE = 2376
        private const val MERCURY_X_2_SIZE = 897
        private const val MERCURY_X_3_SIZE = 162
        private const val MERCURY_X_4_SIZE = 45
        private const val MERCURY_X_5_SIZE = 30
        private const val MERCURY_Y_0_SIZE = 4314
        private const val MERCURY_Y_1_SIZE = 2346
        private const val MERCURY_Y_2_SIZE = 897
        private const val MERCURY_Y_3_SIZE = 177
        private const val MERCURY_Y_4_SIZE = 45
        private const val MERCURY_Y_5_SIZE = 30
        private const val MERCURY_Z_0_SIZE = 1794
        private const val MERCURY_Z_1_SIZE = 1053
        private const val MERCURY_Z_2_SIZE = 429
        private const val MERCURY_Z_3_SIZE = 84
        private const val MERCURY_Z_4_SIZE = 30
        private const val MERCURY_Z_5_SIZE = 21
        private const val NEPTUNE_X_0_SIZE = 2316
        private const val NEPTUNE_X_1_SIZE = 990
        private const val NEPTUNE_X_2_SIZE = 306
        private const val NEPTUNE_X_3_SIZE = 99
        private const val NEPTUNE_X_4_SIZE = 21
        private const val NEPTUNE_Y_0_SIZE = 2238
        private const val NEPTUNE_Y_1_SIZE = 975
        private const val NEPTUNE_Y_2_SIZE = 291
        private const val NEPTUNE_Y_3_SIZE = 102
        private const val NEPTUNE_Y_4_SIZE = 21
        private const val NEPTUNE_Z_0_SIZE = 399
        private const val NEPTUNE_Z_1_SIZE = 111
        private const val NEPTUNE_Z_2_SIZE = 33
        private const val NEPTUNE_Z_3_SIZE = 6
        private const val MARS_X_0_SIZE = 4752
        private const val MARS_X_1_SIZE = 2868
        private const val MARS_X_2_SIZE = 1161
        private const val MARS_X_3_SIZE = 405
        private const val MARS_X_4_SIZE = 123
        private const val MARS_X_5_SIZE = 63
        private const val MARS_Y_0_SIZE = 4836
        private const val MARS_Y_1_SIZE = 2907
        private const val MARS_Y_2_SIZE = 1152
        private const val MARS_Y_3_SIZE = 408
        private const val MARS_Y_4_SIZE = 132
        private const val MARS_Y_5_SIZE = 63
        private const val MARS_Z_0_SIZE = 1065
        private const val MARS_Z_1_SIZE = 696
        private const val MARS_Z_2_SIZE = 366
        private const val MARS_Z_3_SIZE = 153
        private const val MARS_Z_4_SIZE = 48
        private const val MARS_Z_5_SIZE = 21
        private const val SATURN_X_0_SIZE = 4956
        private const val SATURN_X_1_SIZE = 2676
        private const val SATURN_X_2_SIZE = 1443
        private const val SATURN_X_3_SIZE = 645
        private const val SATURN_X_4_SIZE = 261
        private const val SATURN_X_5_SIZE = 93
        private const val SATURN_Y_0_SIZE = 4974
        private const val SATURN_Y_1_SIZE = 2751
        private const val SATURN_Y_2_SIZE = 1395
        private const val SATURN_Y_3_SIZE = 603
        private const val SATURN_Y_4_SIZE = 264
        private const val SATURN_Y_5_SIZE = 96
        private const val SATURN_Z_0_SIZE = 1260
        private const val SATURN_Z_1_SIZE = 651
        private const val SATURN_Z_2_SIZE = 261
        private const val SATURN_Z_3_SIZE = 132
        private const val SATURN_Z_4_SIZE = 57
        private const val SATURN_Z_5_SIZE = 18
        private const val URANUS_X_0_SIZE = 4392
        private const val URANUS_X_1_SIZE = 1947
        private const val URANUS_X_2_SIZE = 747
        private const val URANUS_X_3_SIZE = 252
        private const val URANUS_X_4_SIZE = 36
        private const val URANUS_Y_0_SIZE = 4341
        private const val URANUS_Y_1_SIZE = 1977
        private const val URANUS_Y_2_SIZE = 765
        private const val URANUS_Y_3_SIZE = 240
        private const val URANUS_Y_4_SIZE = 36
        private const val URANUS_Z_0_SIZE = 705
        private const val URANUS_Z_1_SIZE = 294
        private const val URANUS_Z_2_SIZE = 99
        private const val URANUS_Z_3_SIZE = 36
        private const val EMB_X_0_SIZE = 2379
        private const val EMB_X_1_SIZE = 1434
        private const val EMB_X_2_SIZE = 555
        private const val EMB_X_3_SIZE = 54
        private const val EMB_X_4_SIZE = 30
        private const val EMB_X_5_SIZE = 18
        private const val EMB_Y_0_SIZE = 2412
        private const val EMB_Y_1_SIZE = 1446
        private const val EMB_Y_2_SIZE = 552
        private const val EMB_Y_3_SIZE = 51
        private const val EMB_Y_4_SIZE = 30
        private const val EMB_Y_5_SIZE = 18
        private const val EMB_Z_0_SIZE = 462
        private const val EMB_Z_1_SIZE = 339
        private const val EMB_Z_2_SIZE = 138
        private const val EMB_Z_3_SIZE = 30
        private const val EMB_Z_4_SIZE = 12
        private const val EMB_Z_5_SIZE = 6
        private const val JUPITER_X_0_SIZE = 3165
        private const val JUPITER_X_1_SIZE = 1464
        private const val JUPITER_X_2_SIZE = 765
        private const val JUPITER_X_3_SIZE = 420
        private const val JUPITER_X_4_SIZE = 174
        private const val JUPITER_X_5_SIZE = 33
        private const val JUPITER_Y_0_SIZE = 3111
        private const val JUPITER_Y_1_SIZE = 1497
        private const val JUPITER_Y_2_SIZE = 777
        private const val JUPITER_Y_3_SIZE = 408
        private const val JUPITER_Y_4_SIZE = 180
        private const val JUPITER_Y_5_SIZE = 33
        private const val JUPITER_Z_0_SIZE = 648
        private const val JUPITER_Z_1_SIZE = 312
        private const val JUPITER_Z_2_SIZE = 195
        private const val JUPITER_Z_3_SIZE = 81
        private const val JUPITER_Z_4_SIZE = 30
        private const val JUPITER_Z_5_SIZE = 9

        @JvmStatic
        private fun computeSeries(serie: DoubleArray, t: Double): DoubleArray {
            var psum = 0.0
            var vsum = 0.0

            for (i in serie.indices step 3) {
                val bct = serie[i + 1] + serie[i + 2] * t
                psum += serie[i] * cos(bct)
                vsum += -serie[i + 2] * serie[i] * sin(bct)
            }

            return doubleArrayOf(psum, vsum)
        }
    }
}
