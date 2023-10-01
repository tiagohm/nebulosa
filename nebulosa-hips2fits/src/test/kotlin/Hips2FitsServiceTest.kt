import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.doubles.shouldBeExactly
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.nulls.shouldNotBeNull
import nebulosa.fits.dec
import nebulosa.fits.imageHDU
import nebulosa.fits.naxis
import nebulosa.fits.ra
import nebulosa.hips2fits.Hips2FitsService
import nebulosa.hips2fits.HipsSurvey
import nebulosa.math.deg
import nebulosa.math.toDegrees
import nom.tam.fits.Fits
import java.io.ByteArrayInputStream

class Hips2FitsServiceTest : StringSpec() {

    init {
        val service = Hips2FitsService()

        "!query" {
            val bytes = service.query(HipsSurvey("CDS/P/DSS2/red"), 201.36506337683.deg, (-43.01911250808).deg).execute().body().shouldNotBeNull()
            val fits = Fits(ByteArrayInputStream(bytes))
            val hdu = fits.imageHDU(0)?.header.shouldNotBeNull()
            hdu.naxis(1) shouldBeExactly 1200
            hdu.naxis(2) shouldBeExactly 900
            hdu.ra.toDegrees shouldBeExactly 201.36506337683
            hdu.dec.toDegrees shouldBeExactly -43.01911250808
            fits.close()
        }
    }
}
