package nebulosa.api.solver

import nebulosa.api.image.ImageBucket
import nebulosa.api.image.ImageSolved
import nebulosa.astap.plate.solving.AstapPlateSolver
import nebulosa.astrometrynet.nova.NovaAstrometryNetService
import nebulosa.astrometrynet.plate.solving.LocalAstrometryNetPlateSolver
import nebulosa.astrometrynet.plate.solving.NovaAstrometryNetPlateSolver
import nebulosa.math.Angle
import okhttp3.OkHttpClient
import org.springframework.stereotype.Service
import java.nio.file.Path
import java.time.Duration

@Service
class PlateSolverService(
    private val imageBucket: ImageBucket,
    private val httpClient: OkHttpClient,
) {

    fun solveImage(
        options: PlateSolverOptions, path: Path,
        centerRA: Angle, centerDEC: Angle, radius: Angle,
    ): ImageSolved {
        val calibration = solve(options, path, centerRA, centerDEC, radius)
        imageBucket.put(path, calibration)
        return ImageSolved(calibration)
    }

    @Synchronized
    fun solve(
        options: PlateSolverOptions, path: Path,
        centerRA: Angle = 0.0, centerDEC: Angle = 0.0, radius: Angle = 0.0,
    ) = with(options) {
        val plateSolver = when (type) {
            PlateSolverType.ASTAP -> AstapPlateSolver(executablePath!!)
            PlateSolverType.ASTROMETRY_NET -> LocalAstrometryNetPlateSolver(executablePath!!)
            PlateSolverType.ASTROMETRY_NET_ONLINE -> {
                val key = "$apiUrl@$apiKey"
                val service = NOVA_ASTROMETRY_NET_CACHE.getOrPut(key) { NovaAstrometryNetService(apiUrl, httpClient) }
                NovaAstrometryNetPlateSolver(service, apiKey)
            }
        }

        plateSolver.solve(path, null, centerRA, centerDEC, radius, 1, DEFAULT_TIMEOUT)
    }

    companion object {

        @JvmStatic private val DEFAULT_TIMEOUT = Duration.ofMinutes(5)
        @JvmStatic private val NOVA_ASTROMETRY_NET_CACHE = HashMap<String, NovaAstrometryNetService>()
    }
}
