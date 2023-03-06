import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.doubles.shouldBeExactly
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotBeBlank
import nebulosa.astrometrynet.nova.NovaAstrometryNetService
import nebulosa.astrometrynet.nova.Parity
import nebulosa.astrometrynet.nova.Upload
import nebulosa.io.resource
import java.io.File

class AstrometryNetServiceTest : StringSpec() {

    init {
        val file = File.createTempFile("nova", ".jpg").also { resource("ldn673s_block1123.jpg")!!.transferTo(it.outputStream()) }

        val service = NovaAstrometryNetService()

        "login" {
            val session = service.login("XXXXXXXX").execute().body().shouldNotBeNull()
            session.status shouldBe "success"
            session.session.shouldNotBeBlank()
        }
        "!upload from url" {
            val session = service.login("XXXXXXXX").execute().body()!!.session

            val upload = Upload(
                session, "http://apod.nasa.gov/apod/image/1206/ldn673s_block1123.jpg",
                scaleLower = 0.5, scaleUpper = 1.0, centerRA = 290.0, centerDEC = 11.0, radius = 2.0
            )

            val submission = service.uploadFromUrl(upload).execute().body()!!
            submission.status shouldBe "success"
            submission.subId shouldBeGreaterThan 0
        }
        "!upload from file" {
            val session = service.login("XXXXXXXX").execute().body()!!.session

            val upload = Upload(session, scaleLower = 0.5, scaleUpper = 1.0, centerRA = 290.0, centerDEC = 11.0, radius = 2.0)

            val submission = service.uploadFromFile(file, upload).execute().body()!!
            submission.status shouldBe "success"
            submission.subId shouldBeGreaterThan 0
        }
        "submission status" {
            val status = service.submissionStatus(7232358).execute().body()!!
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
        "job status" {
            val status = service.jobStatus(7973139).execute().body()!!
            status.status shouldBe "success"
        }
        "job calibration" {
            val calibration = service.jobCalibration(7973139).execute().body()!!
            calibration.parity shouldBe Parity.NEGATIVE
            calibration.orientation shouldBeExactly 90.0397051079753
            calibration.pixScale shouldBeExactly 2.0675124414774606
            calibration.radius shouldBeExactly 0.36561535148882157
            calibration.ra shouldBeExactly 290.237669307
            calibration.dec shouldBeExactly 11.1397773954
        }
        "wcs" {
            val text = service.wcs(7973139).execute().body()!!
            text shouldContain "SIMPLE"
        }
    }
}
