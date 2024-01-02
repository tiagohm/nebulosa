package nebulosa.api.solver

import java.nio.file.Path

data class PlateSolverOptions(
    val type: PlateSolverType = PlateSolverType.ASTROMETRY_NET_ONLINE,
    val executablePath: Path? = null,
    val downsampleFactor: Int = 0,
    val apiUrl: String = "",
    val apiKey: String = "",
) {

    companion object {

        @JvmStatic val EMPTY = PlateSolverOptions()
    }
}
