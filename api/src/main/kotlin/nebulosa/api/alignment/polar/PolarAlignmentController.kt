package nebulosa.api.alignment.polar

import nebulosa.api.alignment.polar.darv.DARVEvent
import nebulosa.api.alignment.polar.darv.DARVStartRequest
import nebulosa.api.alignment.polar.tppa.TPPAEvent
import nebulosa.api.alignment.polar.tppa.TPPAStartRequest
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.guider.GuideOutput
import nebulosa.indi.device.mount.Mount
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("polar-alignment")
class PolarAlignmentController(
    private val polarAlignmentService: PolarAlignmentService,
) {

    @PutMapping("darv/{camera}/{guideOutput}/start")
    fun darvStart(
        camera: Camera, guideOutput: GuideOutput,
        @RequestBody body: DARVStartRequest,
    ) = polarAlignmentService.darvStart(camera, guideOutput, body)

    @PutMapping("darv/{camera}/stop")
    fun darvStop(camera: Camera) {
        polarAlignmentService.darvStop(camera)
    }

    @GetMapping("darv/{camera}/status")
    fun darvStatus(camera: Camera): DARVEvent? {
        return polarAlignmentService.darvStatus(camera)
    }

    @PutMapping("tppa/{camera}/{mount}/start")
    fun tppaStart(
        camera: Camera, mount: Mount,
        @RequestBody body: TPPAStartRequest,
    ) = polarAlignmentService.tppaStart(camera, mount, body)

    @PutMapping("tppa/{camera}/stop")
    fun tppaStop(camera: Camera) {
        polarAlignmentService.tppaStop(camera)
    }

    @PutMapping("tppa/{camera}/pause")
    fun tppaPause(camera: Camera) {
        polarAlignmentService.tppaPause(camera)
    }

    @PutMapping("tppa/{camera}/unpause")
    fun tppaUnpause(camera: Camera) {
        polarAlignmentService.tppaUnpause(camera)
    }

    @GetMapping("tppa/{camera}/status")
    fun tppaStatus(camera: Camera): TPPAEvent? {
        return polarAlignmentService.tppaStatus(camera)
    }
}
