package nebulosa.api.calibration

import nebulosa.api.beans.annotations.EntityBy
import nebulosa.indi.device.camera.Camera
import org.springframework.web.bind.annotation.*
import java.nio.file.Path

@RestController
@RequestMapping("calibration-frames")
class CalibrationFrameController(
    private val calibrationFrameService: CalibrationFrameService,
) {

    @GetMapping("{camera}")
    fun groups(@EntityBy camera: Camera): List<CalibrationFrameGroup> {
        var id = 0
        val groupedFrames = calibrationFrameService.groupedCalibrationFrames(camera)
        return groupedFrames.map { CalibrationFrameGroup(id++, it.key, it.value) }
    }

    @PutMapping("{camera}")
    fun upload(@EntityBy camera: Camera, @RequestParam path: Path): List<CalibrationFrameEntity> {
        return calibrationFrameService.upload(camera, path)
    }

    @PatchMapping("{id}")
    fun edit(
        @PathVariable id: Long,
        @RequestParam(required = false) path: String? = "",
        @RequestParam enabled: Boolean,
    ) = calibrationFrameService.edit(id, path, enabled)

    @DeleteMapping("{id}")
    fun delete(@PathVariable id: Long) {
        calibrationFrameService.delete(id)
    }
}
