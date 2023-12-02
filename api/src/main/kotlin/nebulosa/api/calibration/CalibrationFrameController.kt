package nebulosa.api.calibration

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("calibration-frames")
class CalibrationFrameController(
    private val calibrationFrameService: CalibrationFrameService,
) {

    @GetMapping
    fun groups(): List<CalibrationFrameGroup> {
        val groupedFrames = calibrationFrameService.groupedCalibrationFrames()
        return groupedFrames.map { CalibrationFrameGroup(it.key, it.value) }
    }
}
