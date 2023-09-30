import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.doubles.shouldBeExactly
import io.kotest.matchers.shouldBe
import nebulosa.math.*
import java.util.*

class AngleTest : StringSpec() {

    init {
        "mas" {
            37000.mas shouldBe (0.000179381 plusOrMinus 1e-8)
            37000.mas.toArcsec shouldBe (37.0 plusOrMinus 0.1)
            37000.mas.toArcmin shouldBe (0.616666667 plusOrMinus 1e-8)
            37000.mas.toDegrees shouldBe (0.01027778 plusOrMinus 1e-8)
            37000.mas.toHours shouldBe (0.00068518 plusOrMinus 1e-8)
        }
        "arcsec" {
            37.arcsec shouldBe (0.000179381 plusOrMinus 1e-8)
            37.arcsec.toMas shouldBe (37000.0 plusOrMinus 0.1)
            37.arcsec.toArcmin shouldBe (0.616666667 plusOrMinus 1e-8)
            37.arcsec.toDegrees shouldBe (0.01027778 plusOrMinus 1e-8)
            37.arcsec.toHours shouldBe (0.00068518 plusOrMinus 1e-8)
        }
        "arcmin" {
            45.arcmin shouldBe (0.01308997 plusOrMinus 1e-8)
            45.arcmin.toMas shouldBe (2700000.0 plusOrMinus 0.1)
            45.arcmin.toArcsec shouldBe (2700.0 plusOrMinus 0.1)
            45.arcmin.toDegrees shouldBe (0.75 plusOrMinus 1e-8)
            45.arcmin.toHours shouldBe (0.05 plusOrMinus 1e-8)
        }
        "degrees" {
            6.deg shouldBe (0.10471975 plusOrMinus 1e-8)
            6.deg.toMas shouldBe (21600000.0 plusOrMinus 0.1)
            6.deg.toArcsec shouldBe (21600.0 plusOrMinus 0.1)
            6.deg.toArcmin shouldBe (360.0 plusOrMinus 0.1)
            6.deg.toHours shouldBe (0.4 plusOrMinus 1e-8)
        }
        "hours" {
            4.hours shouldBe (1.04719755 plusOrMinus 1e-8)
            4.hours.toMas shouldBe (216000000.0 plusOrMinus 0.1)
            4.hours.toArcsec shouldBe (216000.0 plusOrMinus 0.1)
            4.hours.toArcmin shouldBe (3600.0 plusOrMinus 0.1)
            4.hours.toDegrees shouldBe (60.0 plusOrMinus 0.1)
        }
        "dms" {
            DMS(6, 45, 8.0) shouldBe (0.1178485 plusOrMinus 1e-8)
        }
        "plus" {
            (0.5.rad + 0.5.rad) shouldBeExactly 1.0
            (0.5.rad + 0.5) shouldBeExactly 1.0
        }
        "minus" {
            (0.8.rad - 0.5.rad) shouldBe (0.3 plusOrMinus 1e-2)
            (0.8.rad - 0.5) shouldBe (0.3 plusOrMinus 1e-2)
        }
        "times" {
            (0.5.rad * 5) shouldBeExactly 2.5
        }
        "div" {
            (5.0.rad / 5) shouldBeExactly 1.0
            5.0.rad / 5.0.rad shouldBeExactly 1.0
        }
        "rem" {
            (5.0.rad % 5) shouldBeExactly 0.0
            (5.0.rad % 5.0.rad) shouldBeExactly 0.0
        }
        "parse decimal coordinates" {
            "23.5634453".deg.toDegrees shouldBe 23.5634453
            "23.5634453".hours.toDegrees shouldBe 353.4516795
        }
        "parse sexagesimal coordinates" {
            "23 33 48.40308".deg.toDegrees shouldBe 23.5634453
            "23h 33 48.40308".hours.toDegrees shouldBe 353.4516795
            "23 33m 48.40308".deg.toDegrees shouldBe 23.5634453
            "23 33 48.40308s".deg.toDegrees shouldBe 23.5634453
            "23h 33m 48.40308".hours.toDegrees shouldBe 353.4516795
            "23 33m 48.40308s".deg.toDegrees shouldBe 23.5634453
            "23h 33m 48.40308s".hours.toDegrees shouldBe 353.4516795
            "-23° 33m 48.40308s".deg.toDegrees shouldBe -23.5634453
            "  -23   33m   48.40308s  ".deg.toDegrees shouldBe -23.5634453
            "-23 33.806718m".deg.toDegrees shouldBe -23.5634453
            "+23".deg.toDegrees shouldBe 23.0
            "-23".deg.toDegrees shouldBe -23.0
            "23h33m48.40308s".hours.toDegrees shouldBe 353.4516795
            "23h33m 48.40308\"".hours.toDegrees shouldBe 353.4516795
            "23h33'48.40308\"".hours.toDegrees shouldBe 353.4516795
            "-23°33'48.40308\"".hours.toDegrees shouldBe -353.4516795
            "-23°33'48.40308s 67.99".deg.toDegrees shouldBe -23.5634453
            "- 23°33'48.40308s 67.99".deg.toDegrees shouldBe -23.5634453
            "".deg.isFinite().shouldBeFalse()
            "kkk".deg.isFinite().shouldBeFalse()
        }
        "format" {
            val angle = "12h 30 1".hours

            angle.toHours shouldBe (12.5003 plusOrMinus 1e-4)

            AngleFormatter.Builder()
                .degrees()
                .secondsDecimalPlaces(2)
                .separators("°", "'", "\"")
                .locale(Locale.ENGLISH)
                .build()
                .format(angle) shouldBe "+187°30'15.00\""

            AngleFormatter.Builder()
                .hours()
                .secondsDecimalPlaces(1)
                .separators("h", "m", "s")
                .locale(Locale.ENGLISH)
                .build()
                .format(angle) shouldBe "+12h30m01.0s"

            AngleFormatter.Builder()
                .hours()
                .noSign()
                .secondsDecimalPlaces(0)
                .locale(Locale.ENGLISH)
                .build()
                .format(angle) shouldBe "12:30:01"

            AngleFormatter.Builder()
                .hours()
                .noSign()
                .noSeconds()
                .build()
                .format(angle) shouldBe "12:30"

            AngleFormatter.Builder()
                .hours()
                .noSign()
                .noSeconds()
                .separators("h", "m")
                .build()
                .format(angle) shouldBe "12h30m"

            val negativeAngle = "-43 00 45".deg

            AngleFormatter.Builder()
                .degreesFormat("%02d")
                .separators("°", "'", "\"")
                .secondsDecimalPlaces(2)
                .build()
                .format(negativeAngle) shouldBe "-43°00'45.00\""

            AngleFormatter.HMS
                .format(angle) shouldBe "12h30m01.0s"

            AngleFormatter.SIGNED_DMS
                .format(negativeAngle) shouldBe "-043°00'45.0\""

            AngleFormatter.DMS
                .format(angle) shouldBe "187°30'15.0\""

            AngleFormatter.SIGNED_DMS
                .newBuilder()
                .secondsDecimalPlaces(2)
                .degreesFormat("%03d")
                .minutesFormat("%04d")
                .secondsFormat("%02.05f")
                .whitespaced()
                .build()
                .format(negativeAngle) shouldBe "-043 0000 45.00000"

            AngleFormatter.HMS
                .format(0.0) shouldBe "00h00m00.0s"

            AngleFormatter.HMS
                .format(CIRCLE) shouldBe "00h00m00.0s"
        }
        "bug on round seconds" {
            "23h59m60.0s".hours
                .format(AngleFormatter.HMS) shouldBe "00h00m00.0s"

            AngleFormatter.HMS
                .format(Radians(6.283182643402501)) shouldBe "23h59m59.9s"
        }
        "bug on parse Unicode negative sign U+2212" {
            "−29 00 28.1".deg.toDegrees shouldBe -29.007805555555557
        }
    }
}
