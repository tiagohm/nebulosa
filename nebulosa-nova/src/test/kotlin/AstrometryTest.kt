import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import nebulosa.math.Angle.Companion.deg
import nebulosa.math.Angle.Companion.rad
import nebulosa.math.Distance.Companion.au
import nebulosa.nasa.daf.RemoteDaf
import nebulosa.nasa.daf.SourceDaf
import nebulosa.nasa.spk.Spk
import nebulosa.nova.astrometry.*
import nebulosa.nova.position.Barycentric
import nebulosa.time.TDB
import nebulosa.time.TimeJD
import nebulosa.time.TimeYMDHMS
import nebulosa.time.UTC
import java.io.File

class AstrometryTest : StringSpec() {

    init {
        val de441 = Spk(RemoteDaf("https://ssd.jpl.nasa.gov/ftp/eph/planets/bsp/de441.bsp"))
        val mar097 = Spk(RemoteDaf("https://naif.jpl.nasa.gov/pub/naif/generic_kernels/spk/satellites/mar097.bsp"))
        val ura111 = Spk(RemoteDaf("https://naif.jpl.nasa.gov/pub/naif/generic_kernels/spk/satellites/ura111.bsp"))
        val ceresSpk = Spk(SourceDaf(File("../assets/1 Ceres.bsp")))
        val kernel = SpiceKernel(de441, mar097, ura111, ceresSpk)
        val sun = kernel[10]
        val moon = kernel[301]
        val earth = kernel[399]
        val mars = kernel[499]
        val uranus = kernel[799]
        val time = UTC(TimeYMDHMS(2022, 12, 25, 0, 0, 0.0))

        "sun: DE441" {
            val astrometric = earth.at<Barycentric>(time).observe(sun)
            val (ra, dec) = astrometric.equatorial()
            // https://ssd.jpl.nasa.gov/horizons/app.html#/
            ra.rad.normalized.degrees shouldBe (273.092531892 plusOrMinus 1e-9)
            dec.rad.degrees shouldBe (-23.406028751 plusOrMinus 1e-9)
        }
        "moon: DE441" {
            val astrometric = earth.at<Barycentric>(time).observe(moon)
            val (ra, dec) = astrometric.equatorial()
            // https://ssd.jpl.nasa.gov/horizons/app.html#/
            ra.rad.normalized.degrees shouldBe (298.048320422 plusOrMinus 1e-9)
            dec.rad.degrees shouldBe (-25.911860684 plusOrMinus 1e-9)
        }
        "moon: ELPMPP02" {
            val astrometric = earth.at<Barycentric>(time).observe(earth + ELPMPP02)
            val (ra, dec) = astrometric.equatorial()
            // https://ssd.jpl.nasa.gov/horizons/app.html#/
            ra.rad.normalized.degrees shouldBe (298.048320422 plusOrMinus 1e-4)
            dec.rad.degrees shouldBe (-25.911860684 plusOrMinus 1e-5)
        }
        "moon: VSOP87E + ELPMPP02" {
            val astrometric = VSOP87E.EARTH.at<Barycentric>(time).observe(VSOP87E.EARTH + ELPMPP02)
            val (ra, dec) = astrometric.equatorial()
            // https://ssd.jpl.nasa.gov/horizons/app.html#/
            ra.rad.normalized.degrees shouldBe (298.048320422 plusOrMinus 1e-4)
            dec.rad.degrees shouldBe (-25.911860684 plusOrMinus 1e-5)
        }
        "mars: MAR097" {
            val astrometric = earth.at<Barycentric>(time).observe(mars)
            val (ra, dec) = astrometric.equatorial()
            // https://ssd.jpl.nasa.gov/horizons/app.html#/
            ra.rad.normalized.degrees shouldBe (68.127269738 plusOrMinus 1e-7)
            dec.rad.degrees shouldBe (24.681041544 plusOrMinus 1e-7)
        }
        "mars: VSOP87E" {
            val astrometric = VSOP87E.EARTH.at<Barycentric>(time).observe(VSOP87E.MARS)
            val (ra, dec) = astrometric.equatorial()
            // https://ssd.jpl.nasa.gov/horizons/app.html#/
            ra.rad.normalized.degrees shouldBe (68.127269738 plusOrMinus 1e-4)
            dec.rad.degrees shouldBe (24.681041544 plusOrMinus 1e-5)
        }
        "ariel: GUST86" {
            val astrometric = earth.at<Barycentric>(time).observe(uranus + GUST86.ARIEL)
            val (ra, dec) = astrometric.equatorial()
            // https://ssd.jpl.nasa.gov/horizons/app.html#/
            ra.rad.normalized.degrees shouldBe (42.621239755 plusOrMinus 1e-4)
            dec.rad.degrees shouldBe (15.978693112 plusOrMinus 1e-5)
        }
        "ceres: Asteroid" {
            // TODO: Failed ~0.5°
            val ceres = Asteroid(
                semiMajorAxis = 2.769289292143484.au,
                eccentricity = 0.07687465013145245,
                inclination = 10.59127767086216.deg,
                argumentOfPerihelion = 73.80896808746482.deg, // W
                longitudeOfAscendingNode = 80.3011901917491.deg, // OM
                meanAnomaly = 130.3159688200986.deg,
                epoch = TDB(TimeJD(2458849.5)),
            )
            val astrometric = earth.at<Barycentric>(time).observe(sun + ceres)
            val (ra, dec) = astrometric.equatorial()
            // https://ssd.jpl.nasa.gov/horizons/app.html#/
            ra.rad.normalized.degrees shouldBe (185.698485350 plusOrMinus 1e-7)
            dec.rad.degrees shouldBe (9.929601380 plusOrMinus 1e-7)
        }
        "ceres: SPK" {
            val ceres = kernel[2000001]
            val astrometric = earth.at<Barycentric>(time).observe(ceres)
            val (ra, dec) = astrometric.equatorial()
            // https://ssd.jpl.nasa.gov/horizons/app.html#/
            ra.rad.normalized.degrees shouldBe (185.698485350 plusOrMinus 1e-7)
            dec.rad.degrees shouldBe (9.929601380 plusOrMinus 1e-7)
        }
    }
}
