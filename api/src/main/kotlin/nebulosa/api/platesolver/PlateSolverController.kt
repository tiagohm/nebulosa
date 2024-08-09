package nebulosa.api.platesolver

import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*
import java.nio.file.Path

@RestController
@RequestMapping("plate-solver")
class PlateSolverController(
    private val plateSolverService: PlateSolverService,
) {

    @PutMapping("start")
    fun start(
        @RequestParam path: Path,
        @RequestBody @Valid solver: PlateSolverRequest,
    ) = plateSolverService.solveImage(solver, path)

    @PutMapping("stop")
    fun stop() {
        plateSolverService.stopSolver()
    }
}
