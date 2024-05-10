package nebulosa.api.solver

import org.hibernate.validator.constraints.time.DurationMax
import org.hibernate.validator.constraints.time.DurationMin
import org.springframework.boot.convert.DurationUnit
import java.nio.file.Path
import java.time.Duration
import java.time.temporal.ChronoUnit

data class PlateSolverOptions(
    @JvmField val type: PlateSolverType = PlateSolverType.ASTROMETRY_NET_ONLINE,
    @JvmField val executablePath: Path? = null,
    @JvmField val downsampleFactor: Int = 0,
    @JvmField val apiUrl: String = "",
    @JvmField val apiKey: String = "",
    @field:DurationMin(seconds = 0) @field:DurationMax(minutes = 5) @field:DurationUnit(ChronoUnit.SECONDS)
    @JvmField val timeout: Duration = Duration.ZERO,
) {

    companion object {

        @JvmStatic val EMPTY = PlateSolverOptions()
    }
}
