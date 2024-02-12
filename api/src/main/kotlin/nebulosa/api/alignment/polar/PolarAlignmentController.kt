package nebulosa.api.alignment.polar

import nebulosa.api.alignment.polar.darv.DARVStartRequest
import nebulosa.api.alignment.polar.tppa.TPPAStartRequest
import nebulosa.api.beans.converters.indi.DeviceOrEntityParam
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.guide.GuideOutput
import nebulosa.indi.device.mount.Mount
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("polar-alignment")
class PolarAlignmentController(
    private val polarAlignmentService: PolarAlignmentService,
) {

    @PutMapping("darv/{camera}/{guideOutput}/start")
    fun darvStart(
        @DeviceOrEntityParam camera: Camera, @DeviceOrEntityParam guideOutput: GuideOutput,
        @RequestBody body: DARVStartRequest,
    ) = polarAlignmentService.darvStart(camera, guideOutput, body)

    @PutMapping("darv/{id}/stop")
    fun darvStop(@PathVariable id: String) {
        polarAlignmentService.darvStop(id)
    }

    @PutMapping("tppa/{camera}/{mount}/start")
    fun tppaStart(
        @DeviceOrEntityParam camera: Camera, @DeviceOrEntityParam mount: Mount,
        @RequestBody body: TPPAStartRequest,
    ) = polarAlignmentService.tppaStart(camera, mount, body)

    @PutMapping("tppa/{id}/stop")
    fun tppaStop(@PathVariable id: String) {
        polarAlignmentService.tppaStop(id)
    }

    @PutMapping("tppa/{id}/pause")
    fun tppaPause(@PathVariable id: String) {
        polarAlignmentService.tppaPause(id)
    }

    @PutMapping("tppa/{id}/unpause")
    fun tppaUnpause(@PathVariable id: String) {
        polarAlignmentService.tppaUnpause(id)
    }
}
