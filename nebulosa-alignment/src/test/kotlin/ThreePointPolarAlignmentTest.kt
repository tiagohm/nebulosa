import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.doubles.shouldBeExactly
import io.kotest.matchers.shouldBe
import nebulosa.alignment.polar.point.three.PolarErrorDetermination
import nebulosa.alignment.polar.point.three.PolarErrorDetermination.Companion.stenographicProjection
import nebulosa.alignment.polar.point.three.Position
import nebulosa.math.arcsec
import nebulosa.math.deg
import nebulosa.math.hours
import nebulosa.math.toArcsec
import nebulosa.platesolver.PlateSolution
import nebulosa.time.TimeYMDHMS
import nebulosa.time.UTC
import org.junit.jupiter.api.Test

class ThreePointPolarAlignmentTest {

    @Test
    fun position() {
        val a = Position("04:14:08".hours, "-05 26 10".deg, LNG, SLAT, UTC(TimeYMDHMS(2024, 2, 10, 22, 59, 13.3739)))
        val b = Position(a.vector, LNG, SLAT)

        a.topocentric.azimuth shouldBeExactly b.topocentric.azimuth
        a.topocentric.altitude shouldBeExactly b.topocentric.altitude
    }

    @Test
    fun stenographicProjection() {
        val coordinates = doubleArrayOf(5.0.arcsec, 8.0.arcsec)
        val projected = coordinates.stenographicProjection(0.0, 0.0, 100.0, 100.0, 1.0.arcsec, 0.0)
        projected[0] shouldBe (95.0 plusOrMinus 1e-8)
        projected[1] shouldBe (92.0 plusOrMinus 1e-8)
    }

    @Test
    fun destinationCoordinates() {
        val position1 = Position("05 35 18".hours, "-05 23 26".deg, LNG, SLAT, UTC(TimeYMDHMS(2024, 2, 10, 22, 58, 42.4979)))
        val position2 = Position("04 54 45".hours, "-05 24 50".deg, LNG, SLAT, UTC(TimeYMDHMS(2024, 2, 10, 22, 58, 58.1655)))
        val position3 = Position("04 14 08".hours, "-05 26 10".deg, LNG, SLAT, UTC(TimeYMDHMS(2024, 2, 10, 22, 59, 13.3739)))
        val initialFrame = PlateSolution(true, 0.0, 1.0.arcsec, "04 14 08".hours, "-05 26 10".deg, 1280.0, 1024.0)
        val pe = PolarErrorDetermination(initialFrame, position1, position2, position3, LNG, SLAT)

        with(pe.destinationCoordinates(0.0, 0.0)) {
            this[0] shouldBe ("04 14 08".hours plusOrMinus 1e-8)
            this[1] shouldBe ("-05 26 10".deg plusOrMinus 1e-8)
        }
    }

    @Test
    fun perfectlyAligned() {
        val now = UTC(TimeYMDHMS(2024, 10, 20, 0, 34, 47.15))
        val position1 = Position("23 24 57.1".hours, "-022 38 11.6".deg, LNG, SLAT, now)
        val position2 = Position("22 24 53.6".hours, "-022 37 35.5".deg, LNG, SLAT, now)
        val position3 = Position("21 24 50.6".hours, "-022 36 31.5".deg, LNG, SLAT, now)

        val initialFrame = PlateSolution(true, 0.0, 1.0.arcsec, "21 24 50.6".hours, "-022 36 31.5".deg, 1280.0, 1024.0)
        val pe = PolarErrorDetermination(initialFrame, position1, position2, position3, LNG, SLAT)
        val (az, alt) = pe.compute()

        az.toArcsec shouldBe (0.0 plusOrMinus 10.0)
        alt.toArcsec shouldBe (0.0 plusOrMinus 65.0)
    }

    @Test
    fun badSouthernPolarAligned() {
        val now = UTC(TimeYMDHMS(2024, 10, 20, 0, 49, 0.0))
        val position1 = Position("23 24 55.4".hours, "-022°52'13.7".deg, LNG, SLAT, now)
        val position2 = Position("22 24 46.7".hours, "-022°47'14.9".deg, LNG, SLAT, now)
        val position3 = Position("21 24 40.5".hours, "-022°41'05.4".deg, LNG, SLAT, now)
        val initialFrame = PlateSolution(true, 0.0, 1.0.arcsec, "21 24 40.5".hours, "-022°41'05.4".deg, 1280.0, 1024.0)
        val pe = PolarErrorDetermination(initialFrame, position1, position2, position3, LNG, SLAT)
        val (az, alt) = pe.compute()

        az.toArcsec shouldBe (900.0 plusOrMinus 10.0)
        alt.toArcsec shouldBe (900.0 plusOrMinus 60.0)
    }

    @Test
    fun badNorthernPolarAligned() {
        val now = UTC(TimeYMDHMS(2024, 10, 20, 0, 53, 0.0))
        val position1 = Position("23 24 09.0".hours, "-022 51 56.0".deg, LNG, NLAT, now)
        val position2 = Position("22 24 00.5".hours, "-022 46 54.1".deg, LNG, NLAT, now)
        val position3 = Position("21 23 54.4".hours, "-022 40 42.4".deg, LNG, NLAT, now)
        val initialFrame = PlateSolution(true, 0.0, 1.0.arcsec, "21 23 54.4".hours, "-022 40 42.4".deg, 1280.0, 1024.0)
        val pe = PolarErrorDetermination(initialFrame, position1, position2, position3, LNG, NLAT)
        val (az, alt) = pe.compute()

        az.toArcsec shouldBe (900.0 plusOrMinus 25.0)
        alt.toArcsec shouldBe (900.0 plusOrMinus 90.0)
    }

    companion object {

        @JvmStatic private val SLAT = "-022 30 00".deg
        @JvmStatic private val NLAT = "+022 30 00".deg
        @JvmStatic private val LNG = "-045 30 00".deg
    }
}
