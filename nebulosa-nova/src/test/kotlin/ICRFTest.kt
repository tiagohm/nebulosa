import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import nebulosa.math.*
import nebulosa.nova.position.Geoid
import nebulosa.nova.position.ICRF
import nebulosa.test.concat
import nebulosa.test.dataDirectory
import nebulosa.time.IERS
import nebulosa.time.IERSA
import nebulosa.time.TT
import nebulosa.time.TimeYMDHMS
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import kotlin.io.path.inputStream

class ICRFTest {

    @Test
    fun equatorialAtDateToEquatorialJ2000() {
        val ra = 2.15105.deg
        val dec = (-0.4493).deg
        val (raNow, decNow) = ICRF.equatorial(ra, dec, epoch = TT(2459950.0, 0.24436)).equatorial()
        raNow.toDegrees shouldBe (1.85881 plusOrMinus 1e-4)
        decNow.toDegrees shouldBe (-0.5762 plusOrMinus 1e-4)
    }

    @Test
    fun equatorialJ2000ToEquatorialAtDate() {
        val ra = 1.85881.deg
        val dec = (-0.5762).deg
        val (raNow, decNow) = ICRF.equatorial(ra, dec).equatorialAtEpoch(TT(2459950.0, 0.24436))
        raNow.toDegrees shouldBe (2.15105 plusOrMinus 1e-4)
        decNow.toDegrees shouldBe (-0.4493 plusOrMinus 1e-4)
    }

    @Test
    fun horizontal() {
        // Sirius.
        val ra = "06 45 08.91728".hours
        val dec = "-16 42 58.0171".deg

        val latitude = (-23.547500000000003).deg
        val longitude = (-46.63610833333333).deg
        val elevation = 853.m

        val time = TimeYMDHMS(2023, 1, 30, 22)
        val site = Geoid.IERS2010.lonLat(longitude, latitude, elevation)

        val icrf = ICRF.equatorial(ra, dec, time = time, center = site)
        val azAlt = icrf.horizontal()
        azAlt.longitude.normalized.toDegrees shouldBe (90.778 plusOrMinus 1e-1)
        azAlt.latitude.toDegrees shouldBe (44.3538 plusOrMinus 1e-1)
    }

    @Test
    fun hourAngle() {
        val time = TimeYMDHMS(2024, 10, 20, 12)
        val site = Geoid.IERS2010.lonLat((-45.0).deg, (-22.0).deg)
        val icrf = ICRF.equatorial(12.0.hours, (-18.0).deg, time = time, center = site)
        val (ha, dec, distance) = icrf.hourAngle()
        ha.toHours shouldBe (24.0 - 1.0584536120036618 plusOrMinus 1e-4)
        dec.toDegrees shouldBe (-18.137759871296097 plusOrMinus 1e-3)
        distance shouldBe (1.0 plusOrMinus 1e-4)
    }

    companion object {

        @JvmStatic
        @BeforeAll
        fun loadIERS() {
            val iersa = IERSA()
            dataDirectory.concat("finals2000A.all").inputStream().use(iersa::load)
            IERS.attach(iersa)
        }
    }
}
