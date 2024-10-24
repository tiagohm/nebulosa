package nebulosa.astrometrynet.platesolver

import nebulosa.astrometrynet.nova.NovaAstrometryNetService
import nebulosa.astrometrynet.nova.NovaAstrometryNetService.Companion.ANONYMOUS_API_KEY
import nebulosa.astrometrynet.nova.ScaleUnit
import nebulosa.astrometrynet.nova.Session
import nebulosa.astrometrynet.nova.Upload
import nebulosa.fits.FitsHeader
import nebulosa.image.Image
import nebulosa.log.di
import nebulosa.log.i
import nebulosa.log.loggerFor
import nebulosa.math.Angle
import nebulosa.math.toDegrees
import nebulosa.platesolver.PlateSolution
import nebulosa.platesolver.PlateSolver
import nebulosa.platesolver.PlateSolverException
import java.nio.file.Path
import java.time.Duration

data class NovaAstrometryNetPlateSolver(
    private val service: NovaAstrometryNetService,
    private val apiKey: String = "",
    private val focalLength: Double = 0.0, // mm
    private val pixelSize: Double = 0.0, // / µm
    private val scale: Double = if (focalLength <= 0.0) 0.0 else (pixelSize / focalLength) * 206.265,
) : PlateSolver {

    @Volatile private var session: Session? = null
    @Volatile private var lastSessionTime = 0L

    @Synchronized
    private fun renewSession() {
        val currentTime = System.currentTimeMillis()

        if (session == null || lastSessionTime == 0L || currentTime - lastSessionTime >= SESSION_EXPIRATION_TIME) {
            val session = service.login(apiKey.ifBlank { ANONYMOUS_API_KEY }).execute().body()
                ?: throw PlateSolverException("failed to renew session key")

            if (session.status != "success") {
                throw PlateSolverException("failed to renew session key: ${session.errorMessage}")
            }

            this.session = session
            lastSessionTime = currentTime
        }
    }

    override fun solve(
        path: Path?, image: Image?,
        centerRA: Angle, centerDEC: Angle, radius: Angle,
        downsampleFactor: Int, timeout: Duration,
    ): PlateSolution {
        val blind = radius.toDegrees < 0.1 || !centerRA.isFinite() || !centerDEC.isFinite()

        renewSession()

        val upload = Upload(
            session = session!!.session,
            centerRA = if (blind) null else centerRA.toDegrees,
            centerDEC = if (blind) null else centerDEC.toDegrees,
            radius = if (blind) null else radius.toDegrees,
            downsampleFactor = if (downsampleFactor <= 0) 2 else downsampleFactor,
            tweakOrder = 2,
            scaleUnits = ScaleUnit.ARCSEC_PER_PIX,
            scaleLower = if (scale > 0) scale * 0.7 else 0.1,
            scaleUpper = if (scale > 0) scale * 1.3 else 180.0,
        )

        val call = path?.let { service.uploadFromFile(it, upload) }
            ?: image?.let { service.uploadFromImage(it, upload) }
            ?: throw PlateSolverException("failed to submit the file")

        val submission = call.execute().body()!!

        if (submission.status != "success") {
            throw PlateSolverException(submission.errorMessage)
        }

        LOG.i("upload submited. check the status at https://nova.astrometry.net/status/{}", submission.subId)

        var timeLeft = timeout.takeIf { it.toSeconds() > 0 }?.toMillis() ?: 300000L

        while (timeLeft >= 0L && !Thread.currentThread().isInterrupted) {
            val startTime = System.currentTimeMillis()

            val status = service.submissionStatus(submission.subId).execute().body()
                ?: throw PlateSolverException("failed to retrieve submission status")

            if (status.solved && !Thread.currentThread().isInterrupted) {
                LOG.di("retrieving WCS from job. id={}", status.jobs[0])

                val body = service.wcs(status.jobs[0]).execute().body()
                    ?: throw PlateSolverException("failed to retrieve WCS file")

                val header = FitsHeader.from(body)
                val calibration = PlateSolution.from(header)

                LOG.i("astrometry.net solved. calibration={}", calibration)

                return calibration ?: PlateSolution.NO_SOLUTION
            }

            if (!Thread.currentThread().isInterrupted) {
                timeLeft -= System.currentTimeMillis() - startTime + 5000L

                Thread.sleep(5000L)
            }
        }

        throw PlateSolverException("the plate solving took a long time and finished")
    }

    companion object {

        private const val SESSION_EXPIRATION_TIME = 1000L * 60 * 15

        @JvmStatic private val LOG = loggerFor<NovaAstrometryNetService>()
    }
}
