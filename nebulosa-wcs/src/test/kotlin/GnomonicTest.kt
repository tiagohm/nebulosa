import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe

@Suppress("FloatingPointLiteralPrecision")
class GnomonicTest : AbstractWCSTransformTest() {

    override val header = mapOf(
        "SIMPLE" to "T",
        "BITPIX" to -32,
        "NAXIS" to 2,
        "NAXIS1" to 192,
        "NAXIS2" to 192,
        "CTYPE1" to "RA---TAN",
        "CRPIX1" to -2.680658087122E+02,
        "CDELT1" to -6.666666666667E-02,
        "CRVAL1" to 0.000000000000E+00,
        "CTYPE2" to "DEC--TAN",
        "CRPIX2" to -5.630437201085E-01,
        "CDELT2" to 6.666666666667E-02,
        "CRVAL2" to -9.000000000000E+01,
        "LONPOLE" to 1.800000000000E+02,
        "LATPOLE" to -9.000000000000E+01,
        "EQUINOX" to 2.000000000000E+03,
    )

    init {
        "pixel to world" {
            with(wcs.pixelToWorld(1.0, 1.0)) {
                first.degrees shouldBe (270.332836050092965 plusOrMinus 1e-12)
                second.degrees shouldBe (-72.615832318447787 plusOrMinus 1e-12)
            }
            with(wcs.pixelToWorld(192.0, 1.0)) {
                first.degrees shouldBe (270.194657942614356 plusOrMinus 1e-12)
                second.degrees shouldBe (-61.839234812473315 plusOrMinus 1e-12)
            }
            with(wcs.pixelToWorld(192.0, 192.0)) {
                first.degrees shouldBe (292.712012780738235 plusOrMinus 1e-12)
                second.degrees shouldBe (-59.87298900275114 plusOrMinus 1e-12)
            }
            with(wcs.pixelToWorld(1.0, 192.0)) {
                first.degrees shouldBe (305.590262846754229 plusOrMinus 1e-12)
                second.degrees shouldBe (-68.943882979281099 plusOrMinus 1e-12)
            }
        }
        "world to pixel" {
            with(wcs.pixelToWorld(1.0, 1.0)) {
                val (x, y) = wcs.worldToPixel(first, second)
                x shouldBe (1.0 plusOrMinus 1e-12)
                y shouldBe (1.0 plusOrMinus 1e-12)
            }
            with(wcs.pixelToWorld(192.0, 1.0)) {
                val (x, y) = wcs.worldToPixel(first, second)
                x shouldBe (192.0 plusOrMinus 1e-12)
                y shouldBe (1.0 plusOrMinus 1e-12)
            }
            with(wcs.pixelToWorld(192.0, 192.0)) {
                val (x, y) = wcs.worldToPixel(first, second)
                x shouldBe (192.0 plusOrMinus 1e-12)
                y shouldBe (192.0 plusOrMinus 1e-12)
            }
            with(wcs.pixelToWorld(1.0, 192.0)) {
                val (x, y) = wcs.worldToPixel(first, second)
                x shouldBe (1.0 plusOrMinus 1e-12)
                y shouldBe (192.0 plusOrMinus 1e-12)
            }
        }
    }
}
