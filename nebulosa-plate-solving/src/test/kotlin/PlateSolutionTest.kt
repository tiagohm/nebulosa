import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import nebulosa.fits.Header
import nebulosa.math.AngleFormatter
import nebulosa.math.format
import nebulosa.math.toArcsec
import nebulosa.math.toDegrees
import nebulosa.plate.solving.PlateSolution

class PlateSolutionTest : StringSpec() {

    init {
        // Don't have CDELT and CROTA
        val astrometryNet = "SIMPLE  =                    T / Standard FITS file                             " +
                "BITPIX  =                    8 / ASCII or bytes array                           " +
                "NAXIS   =                    0 / Minimal header                                 " +
                "EXTEND  =                    T / There may be FITS ext                          " +
                "WCSAXES =                    2 / no comment                                     " +
                "CTYPE1  = 'RA---TAN' / TAN (gnomic) projection                                  " +
                "CTYPE2  = 'DEC--TAN' / TAN (gnomic) projection                                  " +
                "EQUINOX =               2000.0 / Equatorial coordinates definition (yr)         " +
                "LONPOLE =                180.0 / no comment                                     " +
                "LATPOLE =                  0.0 / no comment                                     " +
                "CRVAL1  =        49.7822831385 / RA  of reference point                         " +
                "CRVAL2  =       -66.5033750324 / DEC of reference point                         " +
                "CRPIX1  =               1036.5 / X reference pixel                              " +
                "CRPIX2  =                705.5 / Y reference pixel                              " +
                "CUNIT1  = 'deg     ' / X pixel scale units                                      " +
                "CUNIT2  = 'deg     ' / Y pixel scale units                                      " +
                "CD1_1   =   -0.000277157283326 / Transformation matrix                          " +
                "CD1_2   =     -0.0002597102968 / no comment                                     " +
                "CD2_1   =      0.0002597102968 / no comment                                     " +
                "CD2_2   =   -0.000277157283326 / no comment                                     " +
                "IMAGEW  =                 2072 / Image width,  in pixels.                       " +
                "IMAGEH  =                 1410 / Image height, in pixels.                       " +
                "END                                                                             "

        "astrometry.net" {
            val header = Header.from(astrometryNet)
            val solution = PlateSolution.from(header).shouldNotBeNull()

            solution.rightAscension.format(AngleFormatter.HMS) shouldBe "03h19m07.7s"
            solution.declination.format(AngleFormatter.SIGNED_DMS) shouldBe "-066°30'12.2\""
            solution.orientation.toDegrees shouldBe (-136.9 plusOrMinus 1e-1)
            solution.scale.toArcsec shouldBe (1.37 plusOrMinus 1e-2)
            solution.radius.toDegrees shouldBe (0.476 plusOrMinus 1e-3)
        }
    }
}
