package nebulosa.api.solver

import org.hibernate.validator.constraints.time.DurationMax
import org.hibernate.validator.constraints.time.DurationMin
import org.springframework.boot.convert.DurationUnit
import java.nio.file.Path
import java.time.Duration
import java.time.temporal.ChronoUnit

data class PlateSolverOptions(
    val type: PlateSolverType = PlateSolverType.ASTROMETRY_NET_ONLINE,
    val executablePath: Path? = null,
    val downsampleFactor: Int = 0,
    val apiUrl: String = "",
    val apiKey: String = "",
    @field:DurationMin(seconds = 0) @field:DurationMax(minutes = 5) @field:DurationUnit(ChronoUnit.SECONDS) val timeout: Duration = Duration.ZERO,
)
