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

    @Synchronized
    fun solve(
        path: Path,
        centerRA: Angle = 0.0, centerDEC: Angle = 0.0, radius: Angle = 0.0,
    ): PlateSolution {
        val settings = preferenceService.plateSolverSettings

        val plateSolver = when (settings.type) {
            PlateSolverType.ASTAP -> AstapPlateSolver(settings.executablePath!!)
            PlateSolverType.ASTROMETRY_NET -> LocalAstrometryNetPlateSolver(settings.executablePath!!)
        }

        return plateSolver.solve(path, centerRA, centerDEC, radius, 2, DEFAULT_TIMEOUT)
    }

    companion object {

        const val PLATE_SOLVER_SETTINGS = "PLATE_SOLVER_SETTINGS"

        @JvmStatic private val DEFAULT_TIMEOUT = Duration.ofMinutes(5)

        inline var PreferenceService.plateSolverSettings
            get() = getJSON<PlateSolverSettings>(PLATE_SOLVER_SETTINGS) ?: PlateSolverSettings()
            set(value) = run { putJSON(PLATE_SOLVER_SETTINGS, value) }
    }
}
