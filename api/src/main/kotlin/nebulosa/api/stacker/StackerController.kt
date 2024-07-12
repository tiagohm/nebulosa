package nebulosa.api.stacker

import jakarta.validation.Valid
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.nio.file.Path

@Validated
@RestController
@RequestMapping("stacker")
class StackerController(private val stackerService: StackerService) {

    @PutMapping
    fun stack(@RequestBody @Valid body: StackingRequest): Path? {
        return stackerService.stack(body)
    }

    @PutMapping("analyze")
    fun analyze(@RequestParam path: Path) {
    }
}
