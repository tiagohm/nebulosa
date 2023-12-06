package nebulosa.api.solver

import jakarta.validation.Valid
import nebulosa.math.deg
import nebulosa.math.hours
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
        @RequestParam(required = false, defaultValue = "0.0") centerRA: String,
        @RequestParam(required = false, defaultValue = "0.0") centerDEC: String,
        @RequestParam(required = false, defaultValue = "8.0") radius: String,
    ) = plateSolverService.solveImage(path, centerRA.hours, centerDEC.deg, if (blind) 0.0 else radius.deg)

    @PutMapping("settings")
    fun settings(@RequestBody @Valid body: PlateSolverOptions) {
        plateSolverService.settings(body)
    }

    @GetMapping("settings")
    fun settings(): PlateSolverOptions {
        return plateSolverService.settings()
    }
}
