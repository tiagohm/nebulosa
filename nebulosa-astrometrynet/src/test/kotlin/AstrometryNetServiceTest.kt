import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.doubles.shouldBeExactly
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotBeBlank
import nebulosa.astrometrynet.nova.NovaAstrometryNetService
import nebulosa.astrometrynet.nova.NovaAstrometryNetService.Companion.ANONYMOUS_API_KEY
import nebulosa.astrometrynet.nova.Parity
import nebulosa.astrometrynet.nova.Upload
import nebulosa.test.concat
import nebulosa.test.dataDirectory
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class AstrometryNetServiceTest {

    @Test
    fun login() {
        val session = SERVICE.login(ANONYMOUS_API_KEY).execute().body().shouldNotBeNull()
        session.status shouldBe "success"
        session.session.shouldNotBeBlank()
    }

    @Test
    @Disabled
    fun uploadFromUrl() {
        val session = SERVICE.login(ANONYMOUS_API_KEY).execute().body()!!.session

        val upload = Upload(
            session, "http://apod.nasa.gov/apod/image/1206/ldn673s_block1123.jpg",
            scaleLower = 0.5, scaleUpper = 1.0, centerRA = 290.0, centerDEC = 11.0, radius = 2.0
        )

        val submission = SERVICE.uploadFromUrl(upload).execute().body()!!
        submission.status shouldBe "success"
        submission.subId shouldBeGreaterThan 0
    }

    @Test
    @Disabled
    fun uploadFromFile() {
        val session = SERVICE.login(ANONYMOUS_API_KEY).execute().body()!!.session
        val upload = Upload(session, scaleLower = 0.5, scaleUpper = 1.0, centerRA = 290.0, centerDEC = 11.0, radius = 2.0)
        val file = dataDirectory.concat("ldn673s_block1123.jpg")
        val submission = SERVICE.uploadFromFile(file, upload).execute().body()!!
        submission.status shouldBe "success"
        submission.subId shouldBeGreaterThan 0
    }

    @Test
    fun submissionStatus() {
        val status = SERVICE.submissionStatus(7232358).execute().body()!!
        status.jobs.size shouldBeExactly 1
        status.jobs[0] shouldBeExactly 7973139
        status.userImages.size shouldBeExactly 1
        status.userImages[0] shouldBeExactly 7402591
        status.jobCalibrations.size shouldBeExactly 1
        status.jobCalibrations[0].size shouldBeExactly 2
        status.jobCalibrations[0][0] shouldBeExactly 7973139
        status.jobCalibrations[0][1] shouldBeExactly 5950379
        status.user shouldBeExactly 1000
        status.started.shouldBeTrue()
        status.solved.shouldBeTrue()
    }

    @Test
    fun jobStatus() {
        val status = SERVICE.jobStatus(7973139).execute().body()!!
        status.status shouldBe "success"
    }

    @Test
    fun jobCalibration() {
        val calibration = SERVICE.jobCalibration(7973139).execute().body()!!
        calibration.parity shouldBe Parity.NEGATIVE
        calibration.orientation shouldBeExactly 90.0397051079753
        calibration.pixScale shouldBeExactly 2.0675124414774606
        calibration.radius shouldBeExactly 0.36561535148882157
        calibration.ra shouldBeExactly 290.237669307
        calibration.dec shouldBeExactly 11.1397773954
    }

    @Test
    fun wcs() {
        val text = SERVICE.wcs(7973139).execute().body()!!
        text.decodeToString() shouldContain "SIMPLE"
    }

    companion object {

        private val SERVICE = NovaAstrometryNetService()
    }
}
