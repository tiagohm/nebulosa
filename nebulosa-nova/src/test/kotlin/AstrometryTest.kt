import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import nebulosa.io.seekableSource
import nebulosa.math.au
import nebulosa.math.deg
import nebulosa.math.normalized
import nebulosa.math.toDegrees
import nebulosa.nasa.daf.RemoteDaf
import nebulosa.nasa.daf.SourceDaf
import nebulosa.nasa.spk.NAIF
import nebulosa.nasa.spk.Spk
import nebulosa.nova.astrometry.*
import nebulosa.nova.position.Barycentric
import nebulosa.test.concat
import nebulosa.test.dataDirectory
import nebulosa.time.TDB
import nebulosa.time.TimeYMDHMS
import nebulosa.time.UTC
import org.junit.jupiter.api.Test

class AstrometryTest {

    @Test
    fun sunDE441() {
        val astrometric = EARTH.at<Barycentric>(TIME).observe(SUN)
        val (ra, dec) = astrometric.equatorial()
        // https://ssd.jpl.nasa.gov/horizons/app.html#/
        ra.normalized.toDegrees shouldBe (273.092531892 plusOrMinus 1e-9)
        dec.toDegrees shouldBe (-23.406028751 plusOrMinus 1e-9)
    }

    @Test
    fun moonDE441() {
        val astrometric = EARTH.at<Barycentric>(TIME).observe(MOON)
        val (ra, dec) = astrometric.equatorial()
        // https://ssd.jpl.nasa.gov/horizons/app.html#/
        ra.normalized.toDegrees shouldBe (298.048320422 plusOrMinus 1e-9)
        dec.toDegrees shouldBe (-25.911860684 plusOrMinus 1e-9)
    }

    @Test
    fun moonELPMPP02() {
        val astrometric = EARTH.at<Barycentric>(TIME).observe(EARTH + ELPMPP02)
        val (ra, dec) = astrometric.equatorial()
        // https://ssd.jpl.nasa.gov/horizons/app.html#/
        ra.normalized.toDegrees shouldBe (298.048320422 plusOrMinus 1e-4)
        dec.toDegrees shouldBe (-25.911860684 plusOrMinus 1e-5)
    }

    @Test
    fun moonVSOP87EAndELPMPP02() {
        val astrometric = VSOP87E.EARTH.at<Barycentric>(TIME).observe(VSOP87E.EARTH + ELPMPP02)
        val (ra, dec) = astrometric.equatorial()
        // https://ssd.jpl.nasa.gov/horizons/app.html#/
        ra.normalized.toDegrees shouldBe (298.048320422 plusOrMinus 1e-4)
        dec.toDegrees shouldBe (-25.911860684 plusOrMinus 1e-5)
    }

    @Test
    fun marsMAR097() {
        val astrometric = EARTH.at<Barycentric>(TIME).observe(MARS)
        val (ra, dec) = astrometric.equatorial()
        // https://ssd.jpl.nasa.gov/horizons/app.html#/
        ra.normalized.toDegrees shouldBe (68.127269738 plusOrMinus 1e-7)
        dec.toDegrees shouldBe (24.681041544 plusOrMinus 1e-7)
    }

    @Test
    fun marsVSOP87E() {
        val astrometric = VSOP87E.EARTH.at<Barycentric>(TIME).observe(VSOP87E.MARS)
        val (ra, dec) = astrometric.equatorial()
        // https://ssd.jpl.nasa.gov/horizons/app.html#/
        ra.normalized.toDegrees shouldBe (68.127269738 plusOrMinus 1e-4)
        dec.toDegrees shouldBe (24.681041544 plusOrMinus 1e-5)
    }

    @Test
    fun arielGUST86() {
        val astrometric = EARTH.at<Barycentric>(TIME).observe(URANUS + GUST86.ARIEL)
        val (ra, dec) = astrometric.equatorial()
        // https://ssd.jpl.nasa.gov/horizons/app.html#/
        ra.normalized.toDegrees shouldBe (42.621239755 plusOrMinus 1e-4)
        dec.toDegrees shouldBe (15.978693112 plusOrMinus 1e-5)
    }

    @Test
    fun ceresAsteroid() {
        val ceres = Asteroid(
            semiMajorAxis = 2.769289292143484.au,
            eccentricity = 0.07687465013145245,
            inclination = 10.59127767086216.deg,
            longitudeOfAscendingNode = 80.3011901917491.deg, // OM
            argumentOfPerihelion = 73.80896808746482.deg, // W
            meanAnomaly = 130.3159688200986.deg,
            epoch = TDB(2458849.5),
        )
        val astrometric = EARTH.at<Barycentric>(TIME).observe(SUN + ceres)
        val (ra, dec) = astrometric.equatorial()
        // https://ssd.jpl.nasa.gov/horizons/app.html#/
        ra.normalized.toDegrees shouldBe (185.698485350 plusOrMinus 0.7)
        dec.toDegrees shouldBe (9.929601380 plusOrMinus 0.3)
    }

    @Test
    fun ceresSPK() {
        val ceres = KERNEL[NAIF.CERES]
        val astrometric = EARTH.at<Barycentric>(TIME).observe(ceres)
        val (ra, dec) = astrometric.equatorial()
        // https://ssd.jpl.nasa.gov/horizons/app.html#/
        ra.normalized.toDegrees shouldBe (185.698485350 plusOrMinus 1e-7)
        dec.toDegrees shouldBe (9.929601380 plusOrMinus 1e-7)
    }

    companion object {

        @JvmStatic private val DE441 = Spk(RemoteDaf("https://ssd.jpl.nasa.gov/ftp/eph/planets/bsp/de441.bsp"))
        @JvmStatic private val MAR097 = Spk(RemoteDaf("https://naif.jpl.nasa.gov/pub/naif/generic_kernels/spk/satellites/mar097.bsp"))
        @JvmStatic private val URA111 = Spk(RemoteDaf("https://naif.jpl.nasa.gov/pub/naif/generic_kernels/spk/satellites/ura111.bsp"))
        @JvmStatic private val CERES = Spk(SourceDaf(dataDirectory.concat("1 Ceres.bsp").seekableSource()))
        @JvmStatic private val KERNEL = SpiceKernel(DE441, MAR097, URA111, CERES)
        @JvmStatic private val SUN = KERNEL[NAIF.SUN]
        @JvmStatic private val MOON = KERNEL[NAIF.MOON]
        @JvmStatic private val EARTH = KERNEL[NAIF.EARTH]
        @JvmStatic private val MARS = KERNEL[NAIF.MARS]
        @JvmStatic private val URANUS = KERNEL[NAIF.URANUS]
        @JvmStatic private val TIME = UTC(TimeYMDHMS(2022, 12, 25, 0, 0, 0.0))
    }
}
