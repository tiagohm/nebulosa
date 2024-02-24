import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.doubles.shouldBeExactly
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.nulls.shouldNotBeNull
import nebulosa.fits.*
import nebulosa.hips2fits.Hips2FitsService
import nebulosa.io.source
import nebulosa.math.deg
import nebulosa.math.toDegrees

class Hips2FitsServiceTest : StringSpec() {

    init {
        val service = Hips2FitsService()

        "query" {
            val responseBody = service
                .query("CDS/P/DSS2/red", 201.36506337683.deg, (-43.01911250808).deg)
                .execute()
                .body()
                .shouldNotBeNull()
            val fits = responseBody.use { it.bytes().source().fits() }
            val hdu = fits.filterIsInstance<ImageHdu>().first().header
            hdu.width shouldBeExactly 1200
            hdu.height shouldBeExactly 900
            hdu.rightAscension.toDegrees shouldBeExactly 201.36506337683
            hdu.declination.toDegrees shouldBeExactly -43.01911250808
        }
        "available surveys" {
            val surveys = service.availableSurveys().execute().body().shouldNotBeNull()
            surveys.shouldNotBeEmpty()
            surveys shouldHaveSize 115
        }
    }
}
