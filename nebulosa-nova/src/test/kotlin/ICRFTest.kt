import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import nebulosa.io.resource
import nebulosa.math.Angle
import nebulosa.math.Angle.Companion.deg
import nebulosa.math.Distance.Companion.m
import nebulosa.nova.position.Geoid
import nebulosa.nova.position.ICRF
import nebulosa.time.IERS
import nebulosa.time.IERSA
import nebulosa.time.TimeJD
import nebulosa.time.TimeYMDHMS

class ICRFTest : StringSpec() {

    init {
        val iersa = IERSA()
        iersa.load(resource("finals2000A.all")!!)
        IERS.attach(iersa)

        "equatorial at date to equatorial J2000" {
            val ra = 2.15105.deg
            val dec = (-0.4493).deg
            val (raNow, decNow) = ICRF.equatorial(ra, dec, epoch = TimeJD(2459950.24436)).equatorialJ2000()
            raNow.degrees shouldBe (1.85881 plusOrMinus 1e-2)
            decNow.degrees shouldBe (-0.5762 plusOrMinus 1e-2)
        }
        "equatorial J2000 to equatorial at date" {
            val ra = 1.85881.deg
            val dec = (-0.5762).deg
            val (raNow, decNow) = ICRF.equatorial(ra, dec).equatorialAtEpoch(TimeJD(2459950.24436))
            raNow.degrees shouldBe (2.15105 plusOrMinus 1e-2)
            decNow.degrees shouldBe (-0.4493 plusOrMinus 1e-2)
        }
        "horizontal" {
            // Sirius.
            val ra = Angle.from("06 45 08.91728", isHours = true)!!
            val dec = Angle.from("-16 42 58.0171")!!

            val latitude = (-23.547500000000003).deg
            val longitude = (-46.63610833333333).deg
            val elevation = 853.m

            val time = TimeYMDHMS(2023, 1, 30, 22)
            val site = Geoid.IERS2010.latLon(longitude, latitude, elevation)

            val icrf = ICRF.equatorial(ra, dec, time = time, center = site)
            val azAlt = icrf.horizontal() // TODO: Apply refraction and retest!
            // azAlt.longitude.normalized.degrees shouldBe (90.778 plusOrMinus 1e-1)
            // azAlt.latitude.degrees shouldBe (44.3538 plusOrMinus 1e-1)
        }
    }
}
