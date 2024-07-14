package nebulosa.api.platesolver

import nebulosa.astap.platesolver.AstapPlateSolver
import nebulosa.astrometrynet.nova.NovaAstrometryNetService
import nebulosa.astrometrynet.platesolver.LocalAstrometryNetPlateSolver
import nebulosa.astrometrynet.platesolver.NovaAstrometryNetPlateSolver
import nebulosa.pixinsight.platesolver.PixInsightPlateSolver
import nebulosa.pixinsight.script.startPixInsight
import nebulosa.siril.platesolver.SirilPlateSolver
import okhttp3.OkHttpClient
import org.hibernate.validator.constraints.time.DurationMax
import org.hibernate.validator.constraints.time.DurationMin
import org.springframework.boot.convert.DurationUnit
import java.nio.file.Path
import java.time.Duration
import java.time.temporal.ChronoUnit

data class PlateSolverRequest(
    @JvmField val type: PlateSolverType = PlateSolverType.ASTROMETRY_NET_ONLINE,
    @JvmField val executablePath: Path? = null,
    @JvmField val downsampleFactor: Int = 0,
    @JvmField val focalLength: Double = 0.0,
    @JvmField val pixelSize: Double = 0.0,
    @JvmField val apiUrl: String = "",
    @JvmField val apiKey: String = "",
    @field:DurationMin(seconds = 0) @field:DurationMax(minutes = 5) @field:DurationUnit(ChronoUnit.SECONDS)
    @JvmField val timeout: Duration = Duration.ZERO,
    @JvmField val slot: Int = 1,
) {

    fun get(httpClient: OkHttpClient? = null) = with(this) {
        when (type) {
            PlateSolverType.ASTAP -> AstapPlateSolver(executablePath!!)
            PlateSolverType.ASTROMETRY_NET -> LocalAstrometryNetPlateSolver(executablePath!!)
            PlateSolverType.ASTROMETRY_NET_ONLINE -> {
                val service = NOVA_ASTROMETRY_NET_CACHE.getOrPut(apiUrl) { NovaAstrometryNetService(apiUrl, httpClient) }
                NovaAstrometryNetPlateSolver(service, apiKey)
            }
            PlateSolverType.SIRIL -> SirilPlateSolver(executablePath!!, focalLength, pixelSize)
            PlateSolverType.PIXINSIGHT -> {
                val runner = startPixInsight(executablePath!!, slot)
                PixInsightPlateSolver(runner, pixelSize, 0.0, focalLength, slot)
            }
        }
    }

    companion object {

        @JvmStatic val EMPTY = PlateSolverRequest()
        @JvmStatic private val NOVA_ASTROMETRY_NET_CACHE = HashMap<String, NovaAstrometryNetService>()
    }
}
