import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import nebulosa.alignment.polar.point.three.PolarErrorDetermination
import nebulosa.alignment.polar.point.three.PolarErrorDetermination.Companion.stenographicProjection
import nebulosa.alignment.polar.point.three.Position
import nebulosa.alignment.polar.point.three.ThreePointPolarAlignment
import nebulosa.erfa.SphericalCoordinate
import nebulosa.math.*
import nebulosa.nova.position.Geoid
import nebulosa.nova.position.ICRF
import nebulosa.platesolver.PlateSolution
import nebulosa.time.TimeYMDHMS
import nebulosa.time.UTC
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import kotlin.random.Random

class ThreePointPolarAlignmentTest {

    @Test
    fun position() {
        val a = Position("04:14:08".hours, "-05 26 10".deg, LNG, SLAT, UTC(TimeYMDHMS(2024, 2, 10, 22, 59, 13.3739)))
        val b = Position(a.vector, LNG, SLAT)

        a.topocentric.azimuth shouldBe (b.topocentric.azimuth plusOrMinus 1e-14)
        a.topocentric.altitude shouldBe (b.topocentric.altitude plusOrMinus 1e-14)
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
        val now = UTC(TimeYMDHMS(2024, 2, 10, 22, 58, 42.4979))
        val position1 = Position("05 35 18".hours, "-05 23 26".deg, LNG, SLAT, now)
        val position2 = Position("04 54 45".hours, "-05 24 50".deg, LNG, SLAT, now)
        val position3 = Position("04 14 08".hours, "-05 26 10".deg, LNG, SLAT, now)
        val initialFrame = PlateSolution(true, 0.0, 1.0.arcsec, "04 14 08".hours, "-05 26 10".deg, 1280.0, 1024.0)
        val pe = PolarErrorDetermination(initialFrame, position1, position2, position3, LNG, SLAT)

        with(pe.destinationCoordinates(0.0, 0.0, now)) {
            this[0] shouldBe ("04 14 08".hours plusOrMinus 1e-11)
            this[1] shouldBe ("-05 26 10".deg plusOrMinus 1e-11)
        }
    }

    @Test
    fun perfectlyAligned() {
        with(computePAE(LNG, SLAT, 0.arcmin, 0.arcmin).first) {
            this[0].toArcsec shouldBe (0.0 plusOrMinus 6.0)
            this[1].toArcsec shouldBe (0.0 plusOrMinus 30.0) // refraction error
        }
    }

    @Test
    fun badSouthernPolarAligned() {
        with(computePAE(LNG, SLAT, 8.arcmin, 8.arcmin).first) {
            this[0].toArcsec shouldBe (480.0 plusOrMinus 6.0)
            this[1].toArcsec shouldBe (480.0 plusOrMinus 30.0)
        }

        with(computePAE(LNG, SLAT, (-8).arcmin, 8.arcmin).first) {
            this[0].toArcsec shouldBe (-480.0 plusOrMinus 6.0)
            this[1].toArcsec shouldBe (480.0 plusOrMinus 30.0)
        }

        with(computePAE(LNG, SLAT, (-8).arcmin, (-8).arcmin).first) {
            this[0].toArcsec shouldBe (-480.0 plusOrMinus 6.0)
            this[1].toArcsec shouldBe (-480.0 plusOrMinus 30.0)
        }

        with(computePAE(LNG, SLAT, 8.arcmin, (-8).arcmin).first) {
            this[0].toArcsec shouldBe (480.0 plusOrMinus 6.0)
            this[1].toArcsec shouldBe (-480.0 plusOrMinus 30.0)
        }

        with(computePAE(LNG, SLAT, 8.arcmin, 0.arcmin).first) {
            this[0].toArcsec shouldBe (480.0 plusOrMinus 6.0)
            this[1].toArcsec shouldBe (0.0 plusOrMinus 30.0)
        }

        with(computePAE(LNG, SLAT, (-8).arcmin, 0.arcmin).first) {
            this[0].toArcsec shouldBe (-480.0 plusOrMinus 6.0)
            this[1].toArcsec shouldBe (0.0 plusOrMinus 30.0)
        }

        with(computePAE(LNG, SLAT, 0.arcmin, (-8).arcmin).first) {
            this[0].toArcsec shouldBe (0.0 plusOrMinus 6.0)
            this[1].toArcsec shouldBe (-480.0 plusOrMinus 30.0)
        }

        with(computePAE(LNG, SLAT, 0.arcmin, (-8).arcmin).first) {
            this[0].toArcsec shouldBe (0.0 plusOrMinus 6.0)
            this[1].toArcsec shouldBe (-480.0 plusOrMinus 30.0)
        }
    }

    @Test
    fun badNorthernPolarAligned() {
        with(computePAE(LNG, NLAT, 8.arcmin, 8.arcmin).first) {
            this[0].toArcsec shouldBe (480.0 plusOrMinus 6.0)
            this[1].toArcsec shouldBe (480.0 plusOrMinus 30.0)
        }

        with(computePAE(LNG, NLAT, (-8).arcmin, 8.arcmin).first) {
            this[0].toArcsec shouldBe (-480.0 plusOrMinus 6.0)
            this[1].toArcsec shouldBe (480.0 plusOrMinus 30.0)
        }

        with(computePAE(LNG, NLAT, (-8).arcmin, (-8).arcmin).first) {
            this[0].toArcsec shouldBe (-480.0 plusOrMinus 6.0)
            this[1].toArcsec shouldBe (-480.0 plusOrMinus 30.0)
        }

        with(computePAE(LNG, NLAT, 8.arcmin, (-8).arcmin).first) {
            this[0].toArcsec shouldBe (480.0 plusOrMinus 6.0)
            this[1].toArcsec shouldBe (-480.0 plusOrMinus 30.0)
        }

        with(computePAE(LNG, NLAT, 8.arcmin, 0.arcmin).first) {
            this[0].toArcsec shouldBe (480.0 plusOrMinus 6.0)
            this[1].toArcsec shouldBe (0.0 plusOrMinus 30.0)
        }

        with(computePAE(LNG, NLAT, (-8).arcmin, 0.arcmin).first) {
            this[0].toArcsec shouldBe (-480.0 plusOrMinus 6.0)
            this[1].toArcsec shouldBe (0.0 plusOrMinus 30.0)
        }

        with(computePAE(LNG, NLAT, 0.arcmin, (-8).arcmin).first) {
            this[0].toArcsec shouldBe (0.0 plusOrMinus 6.0)
            this[1].toArcsec shouldBe (-480.0 plusOrMinus 30.0)
        }

        with(computePAE(LNG, NLAT, 0.arcmin, (-8).arcmin).first) {
            this[0].toArcsec shouldBe (0.0 plusOrMinus 6.0)
            this[1].toArcsec shouldBe (-480.0 plusOrMinus 30.0)
        }
    }

    @Test
    fun tooBadPolarAlignment() {
        with(computePAE(LNG, SLAT, 1.0.deg, 1.0.deg).first) {
            this[0].toArcsec shouldBe (3600.0 plusOrMinus 30.0)
            this[1].toArcsec shouldBe (3600.0 plusOrMinus 90.0)
        }
    }

    @Test
    @Disabled
    fun updateWithoutSeeing() {
        val (pae, ped) = computePAE(LNG, NLAT, 0.arcmin, (-8).arcmin)

        with(pae) {
            this[0].toArcsec shouldBe (0.0 plusOrMinus 6.0)
            this[1].toArcsec shouldBe (-480.0 plusOrMinus 30.0)
        }

        val rightAscension = ped.currentReferenceFrame.rightAscension
        val declination = ped.currentReferenceFrame.declination
        val referenceFrame = PlateSolution(true, 0.0, 1.0.arcsec, rightAscension, declination, 1280.0, 1024.0)

        repeat(500) {
            with(ped.update(PAE_TIME, pae[0], pae[1], referenceFrame)) {
                this[0].toArcsec shouldBe (0.0 plusOrMinus 6.0)
                this[1].toArcsec shouldBe (-480.0 plusOrMinus 30.0)
            }
        }
    }

    @Test
    @Disabled
    fun updateWithSeeing() {
        val (pae, ped) = computePAE(LNG, NLAT, 0.arcmin, (-8).arcmin)

        with(pae) {
            this[0].toArcsec shouldBe (0.0 plusOrMinus 6.0)
            this[1].toArcsec shouldBe (-480.0 plusOrMinus 30.0)
        }

        val currentReferenceFrame = ped.currentReferenceFrame
        val (initialAzimuthError, initialAltitudeError) = pae

        repeat(500) {
            val seeing = Random.nextDouble(-1.0, 1.0).arcsec
            val rightAscension = currentReferenceFrame.rightAscension + seeing
            val declination = currentReferenceFrame.declination + seeing
            val referenceFrame = PlateSolution(true, 0.0, 1.0.arcsec, rightAscension, declination, 1280.0, 1024.0)

            with(ped.update(PAE_TIME, initialAzimuthError, initialAltitudeError, referenceFrame)) {
                this[0].toArcsec shouldBe (0.0 plusOrMinus 65.0)
                this[1].toArcsec shouldBe (-480.0 plusOrMinus 65.0)
            }
        }
    }

    companion object {

        private val SLAT = (-22.0).deg
        private val NLAT = 22.0.deg
        private val LNG = (-45.5).deg

        private val PAE_TIME = UTC(TimeYMDHMS(2024, 10, 20, 12, 0, 0.0))

        private fun computePAE(longitude: Angle, latitude: Angle, azimuthError: Angle, altitudeError: Angle): Pair<DoubleArray, PolarErrorDetermination> {
            val location = Geoid.IERS2010.lonLat(longitude, latitude)
            val rightAscension = location.lstAt(PAE_TIME) // zenith
            val declination = latitude

            fun computePosition(raOffset: Angle): Pair<Position, SphericalCoordinate> {
                val (dRA, dDEC) = ThreePointPolarAlignment.computePAE(-raOffset, declination, latitude, azimuthError, altitudeError)
                val solver = ICRF.equatorial(rightAscension + raOffset + dRA, declination + dDEC, time = PAE_TIME, epoch = PAE_TIME, center = location).equatorial()
                val position = Position(solver.longitude, solver.latitude, longitude, latitude, PAE_TIME)
                return position to solver
            }

            val (position1) = computePosition((-1.0).hours)
            val (position2) = computePosition(0.0.hours)
            val (position3, solver3) = computePosition(1.0.hours)

            val initialFrame = PlateSolution(true, 0.0, 1.0.arcsec, solver3.longitude, solver3.latitude, 1280.0, 1024.0)
            val pe = PolarErrorDetermination(initialFrame, position1, position2, position3, longitude, latitude)

            return pe.compute() to pe
        }
    }
}
