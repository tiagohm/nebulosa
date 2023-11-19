package nebulosa.astrometrynet.plate.solving

import nebulosa.astrometrynet.nova.NovaAstrometryNetService
import nebulosa.astrometrynet.nova.Session
import nebulosa.astrometrynet.nova.Upload
import nebulosa.log.loggerFor
import nebulosa.math.Angle
import nebulosa.math.toDegrees
import nebulosa.plate.solving.PlateSolution
import nebulosa.plate.solving.PlateSolver
import nebulosa.plate.solving.PlateSolvingException
import java.nio.file.Path
import java.time.Duration
import kotlin.math.max

class NovaAstrometryNetPlateSolver(
    private val service: NovaAstrometryNetService,
    private val apiKey: String = "",
) : PlateSolver<Path> {

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
        input: Path, blind: Boolean,
        centerRA: Angle, centerDEC: Angle, radius: Angle,
        downsampleFactor: Int, timeout: Duration?,
    ): PlateSolution {
        renewSession()

        val upload = Upload(
            session = session!!.session,
            centerRA = if (blind) null else centerRA.toDegrees,
            centerDEC = if (blind) null else centerDEC.toDegrees,
            radius = if (blind) null else radius.toDegrees,
            downsampleFactor = downsampleFactor,
        )

        val submission = service.uploadFromFile(input, upload).execute().body()
            ?: throw PlateSolvingException("failed to submit the file")

        if (submission.status != "success") {
            throw PlateSolvingException(submission.errorMessage)
        }

        var timeLeft = max(60000L, timeout?.toMillis() ?: 60000L)

        while (timeLeft >= 0L) {
            val timeStart = System.currentTimeMillis()

            val status = service.submissionStatus(submission.subId).execute().body()
                ?: throw PlateSolvingException("failed to retrieve submission status")

            if (status.solved) {
                val body = service.jobCalibration(status.jobs[0]).execute().body()
                    ?: throw PlateSolvingException("failed to retrieve calibration")

                // TODO:
                // val calibration = Calibration(
                //     body.orientation.deg,
                //     body.pixScale,
                //     body.radius.deg,
                //     body.ra.deg,
                //     body.dec.deg,
                //     body.width / 60.0,
                //     body.height / 60.0,
                // )

                // LOG.info("astrometry.net solved. calibration={}", calibration)

                return PlateSolution()
            }

            val timeEnd = System.currentTimeMillis()
            val timeElapsed = timeEnd - timeStart
            val timeDelay = max(0L, 2000L - timeElapsed)

            if (timeDelay > 0L) Thread.sleep(timeDelay)

            timeLeft -= timeElapsed
            timeLeft -= timeDelay
        }

        throw PlateSolvingException("the plate solving took a long time and finished")
    }

    companion object {

        const val ANONYMOUS_API_KEY = "XXXXXXXX"

        private const val SESSION_EXPIRATION_TIME = 1000L * 60L * 15L

        @JvmStatic private val LOG = loggerFor<NovaAstrometryNetService>()
    }
}
