import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.doubles.shouldBeExactly
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.nulls.shouldNotBeNull
import nebulosa.fits.*
import nebulosa.hips2fits.Hips2FitsService
import nebulosa.image.format.ImageHdu
import nebulosa.io.source
import nebulosa.math.deg
import nebulosa.math.toDegrees
import nebulosa.test.HTTP_CLIENT
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class Hips2FitsServiceTest {

    @Test
    fun query() {
        val responseBody = SERVICE
            .query("CDS/P/DSS2/red", 201.36506337683.deg, (-43.01911250808).deg)
            .execute().body().shouldNotBeNull()
        val fits = responseBody.use { it.bytes().source().fits() }
        val hdu = fits.filterIsInstance<ImageHdu>().first().header
        hdu.width shouldBeExactly 1200
        hdu.height shouldBeExactly 900
        hdu.rightAscension.toDegrees shouldBeExactly 201.36506337683
        hdu.declination.toDegrees shouldBeExactly -43.01911250808
    }

    @Test
    @Disabled
    fun availableSurveys() {
        // Invalid UTF-8 start byte 0xb0. The API returns charset=ISO-8859-1.
        val surveys = SERVICE.availableSurveys().execute().body().shouldNotBeNull()
        surveys.shouldNotBeEmpty()
        surveys shouldHaveSize 115
    }

    companion object {

        @JvmStatic private val SERVICE = Hips2FitsService(httpClient = HTTP_CLIENT)
    }
}
