package nebulosa.api.image

import java.nio.file.Path

data class PlateSolverSettings(
    var type: PlateSolverType = PlateSolverType.ASTAP,
    var executablePath: Path? = null,
    var downsampleFactor: Int = 2,
)
