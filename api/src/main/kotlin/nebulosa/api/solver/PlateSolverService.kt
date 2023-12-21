package nebulosa.api.solver

import com.sun.jna.Platform
import jakarta.annotation.PostConstruct
import nebulosa.api.image.ImageBucket
import nebulosa.api.image.ImageCalibrated
import nebulosa.api.preferences.PreferenceService
import nebulosa.astap.plate.solving.AstapPlateSolver
import nebulosa.astrometrynet.plate.solving.LocalAstrometryNetPlateSolver
import nebulosa.math.Angle
import org.springframework.stereotype.Service
import java.nio.file.Path
import java.time.Duration

@Service
class PlateSolverService(
    private val preferenceService: PreferenceService,
    private val imageBucket: ImageBucket,
) {

    @PostConstruct
    private fun initialize() {
        val settings = settings()

        if (settings.executablePath == null) {
            val executablePath = when {
                Platform.isLinux() -> "astap"
                Platform.isWindows() -> "C:\\Program Files\\astap\\astap.exe"
                else -> "astap"
            }

            settings(settings.copy(executablePath = Path.of(executablePath)))
        }
    }

    fun solveImage(
        path: Path,
        centerRA: Angle, centerDEC: Angle, radius: Angle,
    ): ImageCalibrated {
        val calibration = solve(path, centerRA, centerDEC, radius)
        imageBucket.put(path, calibration)
        return ImageCalibrated(calibration)
    }

    @Synchronized
    fun solve(
        path: Path,
        centerRA: Angle = 0.0, centerDEC: Angle = 0.0, radius: Angle = 0.0,
    ) = with(settings()) {
        val plateSolver = when (type) {
            PlateSolverType.ASTAP -> AstapPlateSolver(executablePath!!)
            PlateSolverType.ASTROMETRY_NET -> LocalAstrometryNetPlateSolver(executablePath!!)
        }

        plateSolver.solve(path, centerRA, centerDEC, radius, 1, DEFAULT_TIMEOUT)
    }

    fun settings(options: PlateSolverOptions) {
        preferenceService.putJSON("SETTINGS.PLATE_SOLVER", options)
    }

    fun settings(): PlateSolverOptions {
        return preferenceService.getJSON<PlateSolverOptions>("SETTINGS.PLATE_SOLVER")
            ?: PlateSolverOptions.EMPTY
    }

    companion object {

        @JvmStatic private val DEFAULT_TIMEOUT = Duration.ofMinutes(5)
    }
}
