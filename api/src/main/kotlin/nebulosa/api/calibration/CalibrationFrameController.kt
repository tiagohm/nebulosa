package nebulosa.api.calibration

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
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

    @GetMapping("{name}")
    fun groupedCalibrationFrames(@PathVariable name: String): List<CalibrationFrameGroup> {
        var id = 0
        val groupedFrames = calibrationFrameService.groupedCalibrationFrames(name)
        return groupedFrames.map { CalibrationFrameGroup(++id, name, it.key, it.value) }
    }

    @PutMapping("{name}")
    fun upload(@PathVariable name: String, @RequestParam path: Path): List<CalibrationFrameEntity> {
        return calibrationFrameService.upload(name, path)
    }

    @PatchMapping("{frame}")
    fun edit(
        frame: CalibrationFrameEntity,
        @Valid @NotBlank @RequestParam name: String, @RequestParam enabled: Boolean,
    ) = calibrationFrameService.edit(frame, name, enabled)

    @DeleteMapping("{frame}")
    fun delete(frame: CalibrationFrameEntity) {
        calibrationFrameService.delete(frame)
    }
}
