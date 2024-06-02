package nebulosa.api.solver

import nebulosa.astap.plate.solving.AstapPlateSolver
import nebulosa.astrometrynet.nova.NovaAstrometryNetService
import nebulosa.astrometrynet.plate.solving.LocalAstrometryNetPlateSolver
import nebulosa.astrometrynet.plate.solving.NovaAstrometryNetPlateSolver
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
    @JvmField val apiUrl: String = "",
    @JvmField val apiKey: String = "",
    @field:DurationMin(seconds = 0) @field:DurationMax(minutes = 5) @field:DurationUnit(ChronoUnit.SECONDS)
    @JvmField val timeout: Duration = Duration.ZERO,
) {

    fun get(httpClient: OkHttpClient? = null) = with(this) {
        when (type) {
            PlateSolverType.ASTAP -> AstapPlateSolver(executablePath!!)
            PlateSolverType.ASTROMETRY_NET -> LocalAstrometryNetPlateSolver(executablePath!!)
            PlateSolverType.ASTROMETRY_NET_ONLINE -> {
                val key = "$apiUrl@$apiKey"
                val service = NOVA_ASTROMETRY_NET_CACHE.getOrPut(key) { NovaAstrometryNetService(apiUrl, httpClient) }
                NovaAstrometryNetPlateSolver(service, apiKey)
            }
        }
    }

    companion object {

        @JvmStatic val EMPTY = PlateSolverRequest()
        @JvmStatic private val NOVA_ASTROMETRY_NET_CACHE = HashMap<String, NovaAstrometryNetService>()
    }
}
