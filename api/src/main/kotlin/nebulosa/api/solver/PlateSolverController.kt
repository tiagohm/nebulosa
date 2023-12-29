package nebulosa.api.solver

import jakarta.validation.Valid
import nebulosa.api.beans.converters.angle.AngleParam
import nebulosa.math.Angle
import org.springframework.web.bind.annotation.*
import java.nio.file.Path

@RestController
@RequestMapping("plate-solver")
class PlateSolverController(
    private val plateSolverService: PlateSolverService,
) {

    @PutMapping
    fun solveImage(
        @RequestParam path: Path,
        @RequestParam(required = false, defaultValue = "true") blind: Boolean,
        @AngleParam(required = false, isHours = true, defaultValue = "0.0") centerRA: Angle,
        @AngleParam(required = false, defaultValue = "0.0") centerDEC: Angle,
        @AngleParam(required = false, defaultValue = "4.0") radius: Angle,
    ) = plateSolverService.solveImage(path, centerRA, centerDEC, if (blind) 0.0 else radius)

    @PutMapping("settings")
    fun settings(@RequestBody @Valid body: PlateSolverOptions) {
        plateSolverService.settings(body)
    }

    @GetMapping("settings")
    fun settings(): PlateSolverOptions {
        return plateSolverService.settings()
    }
}
