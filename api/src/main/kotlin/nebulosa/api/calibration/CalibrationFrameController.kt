package nebulosa.api.calibration

import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("calibration-frames")
class CalibrationFrameController(
    private val calibrationFrameService: CalibrationFrameService,
)
