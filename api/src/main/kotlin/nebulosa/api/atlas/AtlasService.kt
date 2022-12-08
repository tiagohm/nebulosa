package nebulosa.api.atlas

import jakarta.annotation.PostConstruct
import nebulosa.io.seekableSource
import nebulosa.math.Angle
import nebulosa.math.Vector3D
import nebulosa.nasa.daf.RemoteDaf
import nebulosa.nasa.daf.SourceDaf
import nebulosa.nasa.spk.Spk
import nebulosa.nova.astrometry.SpiceKernel
import nebulosa.nova.position.Barycentric
import nebulosa.time.IERS
import nebulosa.time.IERSA
import nebulosa.time.TimeYMDHMS
import nebulosa.time.UTC
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.net.URL
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDateTime
import javax.net.ssl.HttpsURLConnection
import kotlin.io.path.createDirectories
import kotlin.io.path.exists

@Service
class AtlasService {

    @Autowired private lateinit var appDirectory: Path

    private lateinit var kernel: SpiceKernel

    @PostConstruct
    protected fun start() {
        (URL(IERSA.URL).openConnection() as HttpsURLConnection).inputStream.use {
            IERSA.load(it)
            IERS.current = IERSA
        }

        val bspDirectory = Paths.get("$appDirectory", "bsp")

        bspDirectory.createDirectories()

        val spks = ArrayList<Spk>()

        for (file in PSE) {
            val path = Paths.get("$bspDirectory", file.first)

            if (path.exists()) {
                LOG.info("Found SPK file ${file.first} at $path")
                spks.add(Spk(SourceDaf(path.toFile().seekableSource())))
            } else {
                LOG.info("Using remote SPK file ${file.first} at ${file.second}")
                spks.add(Spk(RemoteDaf(file.second)))
            }
        }

        kernel = SpiceKernel(*spks.toTypedArray())
    }

    private val sun get() = kernel[10]

    private val moon get() = kernel[301]

    private val earth get() = kernel[399]

    private fun sun(time: LocalDateTime): Pair<Vector3D, Vector3D> {
        return sun.compute(UTC(TimeYMDHMS(time)))
    }

    fun sun(
        time: LocalDateTime,
        longitude: Angle, latitude: Angle,
    ): Body.Sun {
        val utc = UTC(TimeYMDHMS(time))
        val astrometric = earth.at<Barycentric>(utc).observe(sun)
        val (p, v) = sun.compute(utc)
        val equatorialJ2000 = astrometric.equatorialJ2000()
        val equatorialAtDate = astrometric.equatorialAtDate()

        return Body.Sun(
            p[0], p[1], p[2],
            v[0], v[1], v[2],
            equatorialJ2000.longitude.normalized.degrees, equatorialJ2000.latitude.degrees,
            equatorialAtDate.longitude.normalized.degrees, equatorialAtDate.latitude.degrees,
        )
    }

    private fun moon(time: LocalDateTime): Pair<Vector3D, Vector3D> {
        return moon.compute(UTC(TimeYMDHMS(time)))
    }

    fun moon(
        time: LocalDateTime,
        longitude: Angle, latitude: Angle,
    ): Body.Moon {
        val utc = UTC(TimeYMDHMS(time))
        val astrometric = earth.at<Barycentric>(utc).observe(moon)
        val (p, v) = moon.compute(utc)
        val equatorialJ2000 = astrometric.equatorialJ2000()
        val equatorialAtDate = astrometric.equatorialAtDate()

        return Body.Moon(
            p[0], p[1], p[2],
            v[0], v[1], v[2],
            equatorialJ2000.longitude.normalized.degrees, equatorialJ2000.latitude.degrees,
            equatorialAtDate.longitude.normalized.degrees, equatorialAtDate.latitude.degrees,
        )
    }

    fun planets(
        type: PlanetType,
        time: LocalDateTime,
        longitude: Angle, latitude: Angle,
    ) {

    }

    companion object {

        @JvmStatic private val LOG = LoggerFactory.getLogger(AtlasService::class.java)

        // Planetary Satellite Ephemeris: https://ssd.jpl.nasa.gov/sats/phys_par/sep.html
        const val DE441 = "DE441.bsp"

        /**
         * Satellite Ephemeris: MAR097.
         * ```
         * Ephemeris Version Number: 17
         * Timespan from JED  2415023.500(04-JAN-1900) to JED  2488071.500(03-JAN-2100)
         * File Epoch Julian Date: 2447575.50
         * Master Interval: 4.0000 Days
         * Equinox Reference Julian Date: 2451545.00
         * Planetary Ephemeris Number: DE-0424/LE-0424
         *
         * Bodies on the File:
         *
         *    Name     Number            GM             NDIV   NDEG   Model
         *    Phobos     401    7.087546066894452E-04     16     15   SATORBINT
         *    Deimos     402    9.615569648120313E-05      8     15   SATORBINT
         *    Mars       499    4.282837362069909E+04     16      7   SATORBINT
         *    System            4.282837442560939E+04
         * ```
         */
        const val MAR097 = "MAR097.bsp"

        /**
         * JUP344 Satellite Ephemeris with Jupiter (599) from JUP365 and Jupiter
         * barycenter (5), Sun (10), Earth Moon barycenter (3), and Earth (399)
         * from DE440.
         * ```
         * SPK_KERNEL           = jup344.bsp
         *   SOURCE_SPK_KERNEL  = jup344.bsp.ssd
         *     BODIES           = 55507, 55506, 55505, 55504, 55503, 55502, 55501, 518,
         *                        513, 506, 510, 507, 553, 546, 565, 571, 508, 509, 511,
         *                        512, 517, 519, 520, 521, 522, 523, 524, 525, 526, 527,
         *                        528, 529, 530, 531, 532, 533, 534, 535, 536, 537, 538,
         *                        539, 540, 541, 542, 543, 544, 545, 547, 548, 549, 550,
         *                        551, 552, 554, 555, 556
         *     BEGIN_TIME       = 1799 DEC 26 23:59:18.816
         *     END_TIME         = 2200 JAN 04 23:58:50.816
         *   SOURCE_SPK_KERNEL  = jup365.bsp.ssd
         *     BODIES           = 599
         *     BEGIN_TIME       = 1799 DEC 26 23:59:18.816
         *     END_TIME         = 2200 JAN 04 23:58:50.816
         *   SOURCE_SPK_KERNEL  = de440.bsp
         *     BODIES           = 5, 10, 3, 399
         *     BEGIN_TIME       = 1799 DEC 26 23:59:18.816
         *     END_TIME         = 2200 JAN 04 23:58:50.816
         * ```
         */
        const val JUP344 = "JUP344.bsp"

        /**
         * JUP365 Satellite Ephemeris with Jupiter barycenter (5), Sun (10), Earth
         * Moon barycenter (3), and Earth (399) from DE440.
         * ```
         * SPK_KERNEL           = jup365.bsp
         *   SOURCE_SPK_KERNEL  = jup365.bsp.ssd
         *     BODIES           = 501, 502, 503, 504, 505, 514, 515, 516, 599
         *     BEGIN_TIME       = 1600 JAN 09 23:59:18.816
         *     END_TIME         = 2200 JAN 09 23:58:50.816
         *   SOURCE_SPK_KERNEL  = de440.bsp
         *     BODIES           = 5, 10, 3, 399
         *     BEGIN_TIME       = 1600 JAN 09 23:59:18.816
         *     END_TIME         = 2200 JAN 09 23:58:50.816
         * ```
         */
        const val JUP365 = "JUP365.bsp"

        /**
         * SPK_KERNEL           = sat441.bsp
         *   SOURCE_SPK_KERNEL  = sat441l.bsp
         *     BODIES           = 601, 602, 603, 604, 605, 606, 607, 608, 609, 612, 613,
         *                        614, 634, 632, 699
         *     BEGIN_TIME       = 1749 DEC 29 23:59:18.816
         *     END_TIME         = 2250 JAN 05 23:58:50.816
         *   SOURCE_SPK_KERNEL  = de440.bsp
         *     BODIES           = 6, 10, 3, 399
         *     BEGIN_TIME       = 1749 DEC 29 23:59:18.816
         *     END_TIME         = 2250 JAN 05 23:58:50.816
         */
        const val SAT441 = "SAT441.bsp"

        /**
         * SPK_KERNEL           = sat415.bsp
         *   SOURCE_SPK_KERNEL  = sat415.bsp.ssd
         *     BODIES           = 699, 653, 649, 633, 632, 618, 617, 616, 615, 611, 610
         *     BEGIN_TIME       = 1949 DEC 25 23:59:18.816
         *     END_TIME         = 2050 JAN 09 23:58:50.816
         *   SOURCE_SPK_KERNEL  = de437.bsp.ssd
         *     BODIES           = 6, 10, 3, 399
         *     BEGIN_TIME       = 1949 DEC 25 23:59:18.816
         *     END_TIME         = 2050 JAN 09 23:58:50.816
         */
        const val SAT415 = "SAT415.bsp"

        /**
         * SAT452 Satellite Ephemeris with Saturn (699) from SAT441 and Saturn
         * barycenter (6), Sun (10), Earth Moon barycenter (3), and Earth (399)
         * from DE440.
         * ```
         * SPK_KERNEL           = sat452.bsp
         *   SOURCE_SPK_KERNEL  = sat452.bsp.ssd
         *     BODIES           = 65085, 65086, 65087, 65088, 65079, 65070, 65077, 65067,
         *                        65081, 65082, 65084, 65089, 65090, 65091, 65092, 65093,
         *                        654, 655, 656, 657, 658, 659, 660, 661, 662, 663, 664,
         *                        665, 666
         *     BEGIN_TIME       = 1749 DEC 31 23:59:18.816
         *     END_TIME         = 2149 DEC 22 23:58:50.816
         *   SOURCE_SPK_KERNEL  = sat452.bsp.ssd
         *     BODIES           = 638, 639, 640, 641, 642, 643, 644, 645, 646, 647, 648,
         *                        650, 651, 652, 619, 620, 621, 622, 623, 624, 625, 626,
         *                        627, 628, 629, 630, 631, 636, 637
         *     BEGIN_TIME       = 1749 DEC 29 23:59:18.816
         *     END_TIME         = 2150 JAN 07 23:58:50.816
         *   SOURCE_SPK_KERNEL  = de440.bsp
         *     BODIES           = 6, 10, 3, 399
         *     BEGIN_TIME       = 1749 DEC 29 23:59:18.816
         *     END_TIME         = 2150 JAN 07 23:58:50.816
         *   SOURCE_SPK_KERNEL  = sat441.bsp
         *     BODIES           = 699
         *     BEGIN_TIME       = 1749 DEC 29 23:59:18.816
         *     END_TIME         = 2150 JAN 07 23:58:50.816
         * ```
         */
        const val SAT452 = "SAT452.bsp"
        const val URA111 = "URA111.bsp"
        const val NEP097 = "NEP097.bsp"
        const val PLU043 = "PLU043.bsp"

        @JvmStatic private val PSE = listOf(
            DE441 to "https://ssd.jpl.nasa.gov/ftp/eph/planets/bsp/de441.bsp",
            MAR097 to "https://ssd.jpl.nasa.gov/ftp/eph/satellites/bsp/mar097.bsp",
            JUP344 to "https://ssd.jpl.nasa.gov/ftp/eph/satellites/bsp/jup344.bsp",
            JUP365 to "https://ssd.jpl.nasa.gov/ftp/eph/satellites/bsp/jup365.bsp",
            SAT441 to "https://ssd.jpl.nasa.gov/ftp/eph/satellites/bsp/sat441l.bsp",
            URA111 to "https://ssd.jpl.nasa.gov/ftp/eph/satellites/bsp/ura111.bsp",
            NEP097 to "https://ssd.jpl.nasa.gov/ftp/eph/satellites/bsp/nep097.bsp",
            PLU043 to "https://ssd.jpl.nasa.gov/ftp/eph/satellites/bsp/plu043.bsp",
        )
    }
}
