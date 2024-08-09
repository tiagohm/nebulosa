package nebulosa.api.calibration

import jakarta.validation.Valid
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.nio.file.Path

@Validated
@RestController
@RequestMapping("calibration-frames")
class CalibrationFrameController(
    private val calibrationFrameService: CalibrationFrameService,
) {

    @GetMapping
    fun groups() = calibrationFrameService.groups()

    @GetMapping("{group}")
    fun frames(@PathVariable group: String): List<CalibrationFrameEntity> {
        return calibrationFrameService.frames(group).sorted()
    }

    @PutMapping("{group}")
    fun upload(@PathVariable group: String, @RequestParam path: Path): List<CalibrationFrameEntity> {
        return calibrationFrameService.upload(group, path)
    }

    @PostMapping
    fun update(@RequestBody @Valid body: CalibrationFrameEntity): CalibrationFrameEntity {
        require(body.id > 0L) { "invalid frame id" }
        return calibrationFrameService.edit(body)
    }

    @DeleteMapping("{id}")
    fun delete(@PathVariable id: Long) {
        calibrationFrameService.delete(id)
    }
}
