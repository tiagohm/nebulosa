import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.doubles.shouldBeExactly
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.shouldBe
import nebulosa.math.Angle
import nebulosa.math.Angle.Companion.arcmin
import nebulosa.math.Angle.Companion.arcsec
import nebulosa.math.Angle.Companion.deg
import nebulosa.math.Angle.Companion.hours
import nebulosa.math.Angle.Companion.mas
import nebulosa.math.Angle.Companion.rad

class AngleTest : StringSpec() {

    init {

        timeout = 1000L

        "mas" {
            37000.mas.value shouldBe (0.000179381 plusOrMinus 1e-8)
            37000.mas.arcsec shouldBe (37.0 plusOrMinus 0.1)
            37000.mas.arcmin shouldBe (0.616666667 plusOrMinus 1e-8)
            37000.mas.degrees shouldBe (0.01027778 plusOrMinus 1e-8)
            37000.mas.hours shouldBe (0.00068518 plusOrMinus 1e-8)
        }
        "arcsec" {
            37.arcsec.value shouldBe (0.000179381 plusOrMinus 1e-8)
            37.arcsec.mas shouldBe (37000.0 plusOrMinus 0.1)
            37.arcsec.arcmin shouldBe (0.616666667 plusOrMinus 1e-8)
            37.arcsec.degrees shouldBe (0.01027778 plusOrMinus 1e-8)
            37.arcsec.hours shouldBe (0.00068518 plusOrMinus 1e-8)
        }
        "arcmin" {
            45.arcmin.value shouldBe (0.01308997 plusOrMinus 1e-8)
            45.arcmin.mas shouldBe (2700000.0 plusOrMinus 0.1)
            45.arcmin.arcsec shouldBe (2700.0 plusOrMinus 0.1)
            45.arcmin.degrees shouldBe (0.75 plusOrMinus 1e-8)
            45.arcmin.hours shouldBe (0.05 plusOrMinus 1e-8)
        }
        "degrees" {
            6.deg.value shouldBe (0.10471975 plusOrMinus 1e-8)
            6.deg.mas shouldBe (21600000.0 plusOrMinus 0.1)
            6.deg.arcsec shouldBe (21600.0 plusOrMinus 0.1)
            6.deg.arcmin shouldBe (360.0 plusOrMinus 0.1)
            6.deg.hours shouldBe (0.4 plusOrMinus 1e-8)
        }
        "hours" {
            4.hours.value shouldBe (1.04719755 plusOrMinus 1e-8)
            4.hours.mas shouldBe (216000000.0 plusOrMinus 0.1)
            4.hours.arcsec shouldBe (216000.0 plusOrMinus 0.1)
            4.hours.arcmin shouldBe (3600.0 plusOrMinus 0.1)
            4.hours.degrees shouldBe (60.0 plusOrMinus 0.1)
        }
        "dms" {
            Angle.dms(6, 45, 8.0).value shouldBe (0.1178485 plusOrMinus 1e-8)
        }
        "plus" {
            (0.5.rad + 0.5.rad).value shouldBeExactly 1.0
            (0.5.rad + 0.5).value shouldBeExactly 1.0
        }
        "minus" {
            (0.8.rad - 0.5.rad).value shouldBe (0.3 plusOrMinus 1e-2)
            (0.8.rad - 0.5).value shouldBe (0.3 plusOrMinus 1e-2)
        }
        "times" {
            (0.5.rad * 5).value shouldBeExactly 2.5
        }
        "div" {
            (5.0.rad / 5).value shouldBeExactly 1.0
            5.0.rad / 5.0.rad shouldBeExactly 1.0
        }
        "rem" {
            (5.0.rad % 5).value shouldBeExactly 0.0
            (5.0.rad % 5.0.rad).value shouldBeExactly 0.0
        }
        "compare" {
            Angle.ZERO.compareTo(Angle.SEMICIRCLE) shouldBeExactly -1
            Angle.SEMICIRCLE.compareTo(Angle.CIRCLE) shouldBeExactly -1
            Angle.CIRCLE.compareTo(Angle.ZERO) shouldBeExactly 1
            Angle.ZERO.compareTo(Angle.ZERO) shouldBeExactly 0
        }
    }
}
