import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import nebulosa.math.Angle.Companion.deg

class GnomonicByPixInsightTest : AbstractWCSTransformTest() {

    override val header = mapOf(
        "SIMPLE" to "T",
        "BITPIX" to -32,
        "NAXIS" to 2,
        "NAXIS1" to 2072,
        "NAXIS2" to 1410,
        "EXTEND" to "T",
        "RA" to 83.7787538770004,
        "DEC" to -5.41080773067382,
        "OBJCTRA" to "5 35 06.901",
        "OBJCTDEC" to "-5 24 38.91",
        "CTYPE1" to "RA---TAN",
        "CTYPE2" to "DEC--TAN",
        "CRPIX1" to 1036.39449503,
        "CRPIX2" to 705.66571953,
        "CRVAL1" to 83.7787898844118,
        "CRVAL2" to -5.41087324456594,
        "PV1_1" to 0.0,
        "PV1_2" to 0.0,
        "CD1_1" to 0.0001833516842927,
        "CD1_2" to 0.000333041514971,
        "CD2_1" to 0.0003329920483336,
        "CD2_2" to -0.0001833312886455,
        "CDELT1" to 0.000380122101978975,
        "CDELT2" to -0.000380178569880477,
        "CROTA1" to 61.1651369231946,
        "CROTA2" to 61.1651369231946,
    )

    init {
        "pixel to world" {
            with(wcs.pixelToWorld(1137.105988, 649.456772)) {
                first.degrees shouldBe (83.78159213 plusOrMinus 1e-2)
                second.degrees shouldBe (84.63311739 - 90.0 plusOrMinus 1e-2)
            }
        }
        "world to pixel" {
            with(wcs.worldToPixel(83.77853447.deg, (-5.36703227).deg)) {
                this[0] shouldBe (1137.105988 plusOrMinus 1e-0)
                this[1] shouldBe (649.456772 plusOrMinus 1e-0)
            }
        }
    }
}
