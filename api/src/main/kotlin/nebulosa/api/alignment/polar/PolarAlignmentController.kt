package nebulosa.api.alignment.polar

import nebulosa.api.alignment.polar.darv.DARVStart
import nebulosa.api.beans.annotations.EntityParam
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.guide.GuideOutput
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("polar-alignment")
class PolarAlignmentController(
    private val polarAlignmentService: PolarAlignmentService,
) {

    @PutMapping("darv/{camera}/{guideOutput}/start")
    fun darvStart(
        @EntityParam camera: Camera, @EntityParam guideOutput: GuideOutput,
        @RequestBody body: DARVStart,
    ) {
        polarAlignmentService.darvStart(camera, guideOutput, body)
    }

    @PutMapping("darv/{camera}/{guideOutput}/stop")
    fun darvStop(@EntityParam camera: Camera, @EntityParam guideOutput: GuideOutput) {
        polarAlignmentService.darvStop(camera, guideOutput)
    }
}
