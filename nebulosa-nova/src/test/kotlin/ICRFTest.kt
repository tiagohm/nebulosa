import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import nebulosa.math.Angle.Companion.deg
import nebulosa.math.Angle.Companion.rad
import nebulosa.nova.astrometry.ICRF
import nebulosa.time.TimeJD

class ICRFTest : StringSpec() {

    init {
        "equatorial at date to equatorial J2000" {
            val ra = 2.15105.deg
            val dec = (-0.4493).deg
            val (raNow, decNow) = ICRF.equatorial(ra, dec, epoch = TimeJD(2459950.24436)).equatorialJ2000()
            raNow.rad.degrees shouldBe (1.85881 plusOrMinus 1e-2)
            decNow.rad.degrees shouldBe (-0.5762 plusOrMinus 1e-2)
        }
        "equatorial J2000 to equatorial at date" {
            val ra = 1.85881.deg
            val dec = (-0.5762).deg
            val (raNow, decNow) = ICRF.equatorial(ra, dec).equatorialAtEpoch(TimeJD(2459950.24436))
            raNow.rad.degrees shouldBe (2.15105 plusOrMinus 1e-2)
            decNow.rad.degrees shouldBe (-0.4493 plusOrMinus 1e-2)
        }
    }
}
