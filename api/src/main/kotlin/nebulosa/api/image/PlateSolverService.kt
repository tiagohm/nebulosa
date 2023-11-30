package nebulosa.api.image

import nebulosa.api.preferences.PreferenceService
import nebulosa.astap.plate.solving.AstapPlateSolver
import nebulosa.astrometrynet.plate.solving.LocalAstrometryNetPlateSolver
import nebulosa.math.Angle
import nebulosa.plate.solving.PlateSolution
import org.springframework.stereotype.Service
import java.nio.file.Path
import java.time.Duration

@Service
class PlateSolverService(
    private val preferenceService: PreferenceService,
) {

    fun solve(
        path: Path,
        centerRA: Angle = 0.0, centerDEC: Angle = 0.0, radius: Angle = 0.0,
    ): PlateSolution {
        val type = preferenceService.plateSolverType

        val plateSolver = when (type) {
            PlateSolverType.ASTAP -> AstapPlateSolver(preferenceService.astapPath!!)
            PlateSolverType.ASTROMETRY_NET -> LocalAstrometryNetPlateSolver(preferenceService.astrometryNetPath!!)
        }

        return plateSolver.solve(path, centerRA, centerDEC, radius, 2, DEFAULT_TIMEOUT)
    }

    companion object {

        @JvmStatic private val DEFAULT_TIMEOUT = Duration.ofMinutes(5)
    }
}
