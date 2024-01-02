package nebulosa.astrometrynet.plate.solving

import nebulosa.astrometrynet.nova.NovaAstrometryNetService
import nebulosa.astrometrynet.nova.Session
import nebulosa.astrometrynet.nova.Upload
import nebulosa.fits.Header
import nebulosa.imaging.Image
import nebulosa.log.loggerFor
import nebulosa.math.Angle
import nebulosa.math.toDegrees
import nebulosa.plate.solving.PlateSolution
import nebulosa.plate.solving.PlateSolver
import nebulosa.plate.solving.PlateSolvingException
import java.nio.file.Path
import java.time.Duration
import kotlin.math.max

data class NovaAstrometryNetPlateSolver(
    private val service: NovaAstrometryNetService,
    private val apiKey: String = "",
) : PlateSolver {

    @Volatile private var session: Session? = null
    @Volatile private var lastSessionTime = 0L

    @Synchronized
    private fun renewSession() {
        val currentTime = System.currentTimeMillis()

        if (session == null || lastSessionTime == 0L || currentTime - lastSessionTime >= SESSION_EXPIRATION_TIME) {
            val session = service.login(apiKey.ifBlank { ANONYMOUS_API_KEY }).execute().body()
                ?: throw PlateSolvingException("failed to renew session key")

            if (session.status != "success") {
                throw PlateSolvingException("failed to renew session key: ${session.errorMessage}")
            }

            this.session = session
            lastSessionTime = currentTime
        }
    }

    override fun solve(
        path: Path?, image: Image?,
        centerRA: Angle, centerDEC: Angle, radius: Angle,
        downsampleFactor: Int, timeout: Duration?,
    ): PlateSolution {
        renewSession()

        val blind = radius.toDegrees < 0.1

        val upload = Upload(
            session = session!!.session,
            centerRA = if (blind) null else centerRA.toDegrees,
            centerDEC = if (blind) null else centerDEC.toDegrees,
            radius = if (blind) null else radius.toDegrees,
            downsampleFactor = downsampleFactor,
            tweakOrder = 2,
        )

        val call = path?.let { service.uploadFromFile(it, upload) }
            ?: image?.let { service.uploadFromImage(it, upload) }
            ?: throw PlateSolvingException("failed to submit the file")

        val submission = call.execute().body()!!

        if (submission.status != "success") {
            throw PlateSolvingException(submission.errorMessage)
        }

        var timeLeft = max(60000L, timeout?.toMillis() ?: 0L)

        while (timeLeft >= 0L) {
            val startTime = System.currentTimeMillis()

            val status = service.submissionStatus(submission.subId).execute().body()
                ?: throw PlateSolvingException("failed to retrieve submission status")

            if (status.solved) {
                LOG.info("retrieving WCS from job. id={}", status.jobs[0])

                val body = service.wcs(status.jobs[0]).execute().body()
                    ?: throw PlateSolvingException("failed to retrieve WCS file")

                val header = Header.from(body)
                val calibration = PlateSolution.from(header)

                LOG.info("astrometry.net solved. calibration={}", calibration)

                return calibration ?: PlateSolution.NO_SOLUTION
            }

            timeLeft -= System.currentTimeMillis() - startTime + 5000L

            Thread.sleep(5000L)
        }

        throw PlateSolvingException("the plate solving took a long time and finished")
    }

    companion object {

        const val ANONYMOUS_API_KEY = "XXXXXXXX"

        private const val SESSION_EXPIRATION_TIME = 1000L * 60 * 15

        @JvmStatic private val LOG = loggerFor<NovaAstrometryNetService>()
    }
}
