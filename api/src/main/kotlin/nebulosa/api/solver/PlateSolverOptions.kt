package nebulosa.api.solver

import java.nio.file.Path

data class PlateSolverOptions(
    val type: PlateSolverType = PlateSolverType.ASTAP,
    val executablePath: Path? = null,
    val downsampleFactor: Int = 2,
) {

    companion object {

        @JvmStatic val EMPTY = PlateSolverOptions()
    }
}