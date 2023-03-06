import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import nebulosa.math.Angle.Companion.deg
import nom.tam.fits.Header

class GnomonicByPixInsightTest : AbstractWCSTransformTest() {

    override val header = Header(
        arrayOf(
            "SIMPLE  =                      T",
            "BITPIX  =                    -32",
            "NAXIS   =                      2",
            "NAXIS1  =                   2072",
            "NAXIS2  =                   1410",
            "EXTEND  =                      T",
            "RA      =       83.7787538770004",
            "DEC     =      -5.41080773067382",
            "OBJCTRA =          '5 35 06.901'",
            "OBJCTDEC=          '-5 24 38.91'",
            "CTYPE1  =             'RA---TAN'",
            "CTYPE2  =             'DEC--TAN'",
            "CRPIX1  =          1036.39449503",
            "CRPIX2  =           705.66571953",
            "CRVAL1  =       83.7787898844118",
            "CRVAL2  =      -5.41087324456594",
            "PV1_1   =                    0.0",
            "PV1_2   =                    0.0",
            "CD1_1   =     0.0001833516842927",
            "CD1_2   =      0.000333041514971",
            "CD2_1   =     0.0003329920483336",
            "CD2_2   =    -0.0001833312886455",
            "CDELT1  =   0.000380122101978975",
            "CDELT2  =  -0.000380178569880477",
            "CROTA1  =       61.1651369231946",
            "CROTA2  =       61.1651369231946",
        )
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
